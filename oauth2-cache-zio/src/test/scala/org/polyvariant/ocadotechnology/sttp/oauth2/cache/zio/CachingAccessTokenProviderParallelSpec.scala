package org.polyvariant.sttp.oauth2.cache.zio

import org.polyvariant.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import org.polyvariant.sttp.oauth2.Secret
import org.polyvariant.sttp.oauth2.cache.ExpiringCache
import org.polyvariant.sttp.oauth2.cache.zio.CachingAccessTokenProvider.TokenWithExpirationTime
import org.polyvariant.sttp.oauth2.common.Scope
import zio.test._
import zio.{Duration => ZDuration}
import zio.Ref
import zio.Task
import zio.ZIO

import java.time.Instant
import scala.concurrent.duration._

object CachingAccessTokenProviderParallelSpec extends ZIOSpecDefault {

  private val testScope: Option[Scope] = Scope.of("test-scope")
  private val token = AccessTokenResponse(Secret("secret"), None, 10.seconds, testScope)

  private val sleepDuration: FiniteDuration = 1.second

  def spec = suite("CachingAccessTokenProvider")(
    test("block multiple parallel") {
      prepareTest.flatMap { case (delegate, cachingProvider) =>
        delegate.setToken(testScope, token) *>
          (cachingProvider.requestToken(testScope) zipPar cachingProvider.requestToken(testScope)).map { case (result1, result2) =>
            assert(result1)(Assertion.equalTo(token.copy(expiresIn = result1.expiresIn))) &&
            assert(result2)(Assertion.equalTo(token.copy(expiresIn = result2.expiresIn))) &&
            // if both calls would be made in parallel, both would get the same expiresIn from TestAccessTokenProvider.
            // When blocking is in place, the second call would be delayed by sleepDuration and would hit the cache,
            // which has Instant on top of which new expiresIn would be calculated
            assert(diffInExpirations(result1, result2))(Assertion.isGreaterThanEqualTo(sleepDuration))
          }
      }
    },
    test("not block multiple parallel access if its already in cache") {
      prepareTest.flatMap { case (delegate, cachingProvider) =>
        delegate.setToken(testScope, token) *> cachingProvider.requestToken(testScope) *>
          (cachingProvider.requestToken(testScope) zipPar cachingProvider.requestToken(testScope)) map { case (result1, result2) =>
            assert(result1)(Assertion.equalTo(token.copy(expiresIn = result1.expiresIn))) &&
            assert(result2)(Assertion.equalTo(token.copy(expiresIn = result2.expiresIn))) &&
            // second call should not be forced to wait sleepDuration, because some active token is already in cache
            assert(diffInExpirations(result1, result2))(Assertion.isLessThan(sleepDuration))
          }
      }
    }
  ) @@ TestAspect.withLiveEnvironment

  private def diffInExpirations(result1: AccessTokenResponse, result2: AccessTokenResponse) =
    if (result1.expiresIn > result2.expiresIn) result1.expiresIn - result2.expiresIn else result2.expiresIn - result1.expiresIn

  class DelayingCache[K, V](delegate: ExpiringCache[Task, K, V]) extends ExpiringCache[Task, K, V] {
    override def get(key: K): Task[Option[V]] = delegate.get(key)

    override def put(key: K, value: V, expirationTime: Instant): Task[Unit] =
      ZIO.sleep(ZDuration.fromScala(sleepDuration)) *> delegate.put(key, value, expirationTime)

    override def remove(key: K): Task[Unit] = delegate.remove(key)
  }

  private def prepareTest =
    for {
      state           <- Ref.make[TestAccessTokenProvider.State](TestAccessTokenProvider.State.empty)
      delegate = TestAccessTokenProvider(state)
      cache           <- ZioRefExpiringCache[Option[Scope], TokenWithExpirationTime]
      delayingCache = new DelayingCache(cache)
      cachingProvider <- CachingAccessTokenProvider(delegate, delayingCache)
    } yield (delegate, cachingProvider)

}
