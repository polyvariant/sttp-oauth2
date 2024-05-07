package org.polyvariant.sttp.oauth2.cache.ce2

import cats.FlatMap
import cats.effect.IO
import cats.syntax.all._
import org.polyvariant.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import org.polyvariant.sttp.oauth2.cache.ExpiringCache
import org.polyvariant.sttp.oauth2.cache.ce2.CachingAccessTokenProvider.TokenWithExpirationTime
import org.polyvariant.sttp.oauth2.common.Scope
import org.polyvariant.sttp.oauth2.AccessTokenProvider
import org.polyvariant.sttp.oauth2.Secret
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import cats.effect.{ Ref, Temporal }

class CachingAccessTokenProviderParallelSpec extends AnyWordSpec with Matchers {
  private val ec: ExecutionContext = ExecutionContext.global
  implicit lazy val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val clock: Temporal[IO] = IO.timer(ec)

  private val testScope: Option[Scope] = Scope.of("test-scope")
  private val token = AccessTokenResponse(Secret("secret"), None, 10.seconds, testScope)

  private val sleepDuration: FiniteDuration = 1.second

  "CachingAccessTokenProvider" should {
    "block multiple parallel" in runTest { case (delegate, cachingProvider) =>
      delegate.setToken(testScope, token) *>
        (cachingProvider.requestToken(testScope), cachingProvider.requestToken(testScope)).parTupled map { case (result1, result2) =>
          result1 shouldBe token.copy(expiresIn = result1.expiresIn)
          result2 shouldBe token.copy(expiresIn = result2.expiresIn)
          // if both calls would be made in parallel, both would get the same expiresIn from TestAccessTokenProvider.
          // When blocking is in place, the second call would be delayed by sleepDuration and would hit the cache,
          // which has Instant on top of which new expiresIn would be calculated
          diffInExpirations(result1, result2) shouldBe >=(sleepDuration)
        }
    }

    "not block multiple parallel access if its already in cache" in runTest { case (delegate, cachingProvider) =>
      delegate.setToken(testScope, token) *> cachingProvider.requestToken(testScope) *>
        (cachingProvider.requestToken(testScope), cachingProvider.requestToken(testScope)).parTupled map { case (result1, result2) =>
          result1 shouldBe token.copy(expiresIn = result1.expiresIn)
          result2 shouldBe token.copy(expiresIn = result2.expiresIn)
          // second call should not be forced to wait sleepDuration, because some active token is already in cache
          diffInExpirations(result1, result2) shouldBe <(sleepDuration)
        }
    }
  }

  private def diffInExpirations(result1: AccessTokenResponse, result2: AccessTokenResponse) =
    if (result1.expiresIn > result2.expiresIn) result1.expiresIn - result2.expiresIn else result2.expiresIn - result1.expiresIn

  def runTest(test: ((TestAccessTokenProvider[IO], AccessTokenProvider[IO])) => IO[Assertion]): Assertion =
    prepareTest.flatMap(test).unsafeRunSync()

  class DelayingCache[F[_]: Timer: FlatMap, K, V](delegate: ExpiringCache[F, K, V]) extends ExpiringCache[F, K, V] {
    override def get(key: K): F[Option[V]] = delegate.get(key)

    override def put(key: K, value: V, expirationTime: Instant): F[Unit] =
      Temporal[F].sleep(sleepDuration) *> delegate.put(key, value, expirationTime)

    override def remove(key: K): F[Unit] = delegate.remove(key)
  }

  private def prepareTest =
    for {
      state           <- Ref.of[IO, TestAccessTokenProvider.State](TestAccessTokenProvider.State.empty)
      delegate = TestAccessTokenProvider[IO](state)
      cache           <- CatsRefExpiringCache[IO, Option[Scope], TokenWithExpirationTime]
      delayingCache = new DelayingCache(cache)
      cachingProvider <- CachingAccessTokenProvider[IO](delegate, delayingCache)
    } yield (delegate, cachingProvider)

}
