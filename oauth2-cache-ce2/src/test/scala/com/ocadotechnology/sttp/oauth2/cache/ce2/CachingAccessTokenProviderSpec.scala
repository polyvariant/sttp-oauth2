package org.polyvariant.sttp.oauth2.cache.ce2

import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Timer
import cats.effect.concurrent.Ref
import cats.effect.laws.util.TestContext
import org.polyvariant.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import org.polyvariant.sttp.oauth2.Secret
import org.polyvariant.sttp.oauth2.cache.ce2.CachingAccessTokenProvider.TokenWithExpirationTime
import org.polyvariant.sttp.oauth2.common.Scope
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.Assertion

import scala.concurrent.duration._
import org.polyvariant.sttp.oauth2.AccessTokenProvider

class CachingAccessTokenProviderSpec extends AnyWordSpec with Matchers {
  implicit lazy val testContext: TestContext = TestContext.apply()
  implicit lazy val cs: ContextShift[IO] = IO.contextShift(testContext)
  implicit lazy val ioTimer: Timer[IO] = testContext.timer[IO]

  private val testScope: Option[Scope] = Scope.of("test-scope")
  private val token = AccessTokenResponse(Secret("secret"), None, 10.seconds, testScope)
  private val newToken = AccessTokenResponse(Secret("secret2"), None, 20.seconds, testScope)

  "CachingAccessTokenProvider" should {
    "delegate token retrieval on first call" in runTest { case (delegate, cachingProvider) =>
      for {
        _      <- delegate.setToken(testScope, token)
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe token
    }

    "decrease expiresIn in second read" in runTest { case (delegate, cachingProvider) =>
      for {
        _      <- delegate.setToken(testScope, token)
        _      <- cachingProvider.requestToken(testScope)
        _ = testContext.tick(3.seconds)
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe token.copy(expiresIn = 7.seconds)
    }

    "not refresh token before expiration" in runTest { case (delegate, cachingProvider) =>
      for {
        _      <- delegate.setToken(testScope, token)
        _      <- cachingProvider.requestToken(testScope)
        _      <- delegate.setToken(testScope, newToken)
        _ = testContext.tick(10.seconds - 1.milli)
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe token.copy(expiresIn = 1.milli)
    }

    "ask for token again after expiration" in runTest { case (delegate, cachingProvider) =>
      for {
        _      <- delegate.setToken(testScope, token)
        _      <- cachingProvider.requestToken(testScope)
        _      <- delegate.setToken(testScope, newToken)
        _ = testContext.tick(11.seconds)
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe newToken
    }

  }

  def runTest(test: ((TestAccessTokenProvider[IO], AccessTokenProvider[IO])) => IO[Assertion]): Assertion =
    prepareTest.flatMap(test).unsafeRunSync()

  private def prepareTest =
    for {
      state           <- Ref.of[IO, TestAccessTokenProvider.State](TestAccessTokenProvider.State.empty)
      delegate = TestAccessTokenProvider[IO](state)
      cache           <- CatsRefExpiringCache[IO, Option[Scope], TokenWithExpirationTime]
      cachingProvider <- CachingAccessTokenProvider[IO](delegate, cache)
    } yield (delegate, cachingProvider)

}
