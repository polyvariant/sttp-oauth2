package com.ocadotechnology.sttp.oauth2.cache.cats

import cats.effect.IO
import cats.effect.Ref
import cats.effect.kernel.Outcome.Succeeded
import cats.effect.testkit.TestContext
import cats.effect.testkit.TestInstances
import com.ocadotechnology.sttp.oauth2.AccessTokenProvider
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.cache.cats.CachingAccessTokenProvider.TokenWithExpirationTime
import com.ocadotechnology.sttp.oauth2.common.Scope
import eu.timepit.refined.auto._
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

class CachingAccessTokenProviderSpec extends AnyWordSpec with Matchers with TestInstances {
  private implicit val ticker: Ticker = Ticker(TestContext())

  private val testScope: Scope = "test-scope"
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
        _      <- IO.sleep(3.seconds)
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe token.copy(expiresIn = 7.seconds)
    }

    "not refresh token before expiration" in runTest { case (delegate, cachingProvider) =>
      for {
        _      <- delegate.setToken(testScope, token)
        _      <- cachingProvider.requestToken(testScope)
        _      <- delegate.setToken(testScope, newToken)
        _      <- IO.sleep(10.seconds - 1.milli)
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe token.copy(expiresIn = 1.milli)
    }

    "ask for token again after expiration" in runTest { case (delegate, cachingProvider) =>
      for {
        _      <- delegate.setToken(testScope, token)
        _      <- cachingProvider.requestToken(testScope)
        _      <- delegate.setToken(testScope, newToken)
        _      <- IO.sleep(11.seconds)
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe newToken
    }

  }

  def runTest(test: ((TestAccessTokenProvider[IO], AccessTokenProvider[IO])) => IO[Assertion]): Assertion =
    unsafeRun(prepareTest.flatMap(test)) match {
      case Succeeded(Some(assertion)) => assertion
      case wrongResult                => fail(s"Test should finish successfully. Instead ended with $wrongResult")
    }

  private def prepareTest: IO[(TestAccessTokenProvider[IO], CachingAccessTokenProvider[IO])] =
    for {
      state           <- Ref.of[IO, TestAccessTokenProvider.State](TestAccessTokenProvider.State.empty)
      delegate = TestAccessTokenProvider[IO](state)
      cache           <- CatsRefExpiringCache[IO, Scope, TokenWithExpirationTime]
      cachingProvider <- CachingAccessTokenProvider[IO](delegate, cache)
    } yield (delegate, cachingProvider)

}
