package com.ocadotechnology.sttp.oauth2.cache.future

import com.ocadotechnology.sttp.oauth2.AccessTokenProvider
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.cache.future.FutureCachingAccessTokenProvider.TokenWithExpirationTime
import com.ocadotechnology.sttp.oauth2.common.Scope
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.time.Instant
import scala.concurrent.Future
import scala.concurrent.duration._

class FutureCachingAccessTokenProviderSpec extends AsyncWordSpec with Matchers {

  private val testScope: Option[Scope] = Scope.of("test-scope")
  private val someTime = Instant.parse("2021-10-03T10:15:30.00Z")
  private val token = AccessTokenResponse(Secret("secret"), None, 10.seconds, testScope)
  private val newToken = AccessTokenResponse(Secret("secret2"), None, 20.seconds, testScope)

  "CachingAccessTokenProvider" should {
    "delegate token retrieval on first call" in runTest { case (delegate, cachingProvider, _) =>
      for {
        _      <- delegate.setToken(testScope, token)
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe token
    }

    "decrease expiresIn in second read" in runTest { case (delegate, cachingProvider, timeProvider) =>
      for {
        _      <- delegate.setToken(testScope, token)
        _      <- cachingProvider.requestToken(testScope)
        _ = timeProvider.updateInstant(someTime.plusSeconds(3))
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe token.copy(expiresIn = 7.seconds)
    }

    "not refresh token before expiration" in runTest { case (delegate, cachingProvider, timeProvider) =>
      for {
        _      <- delegate.setToken(testScope, token)
        _      <- cachingProvider.requestToken(testScope)
        _      <- delegate.setToken(testScope, newToken)
        _ = timeProvider.updateInstant(someTime.plusSeconds(10).minusMillis(1))
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe token.copy(expiresIn = 1.milli)
    }

    "ask for token again after expiration" in runTest { case (delegate, cachingProvider, timeProvider) =>
      for {
        _      <- delegate.setToken(testScope, token)
        _      <- cachingProvider.requestToken(testScope)
        _      <- delegate.setToken(testScope, newToken)
        _ = timeProvider.updateInstant(someTime.plusSeconds(11))
        result <- cachingProvider.requestToken(testScope)
      } yield result shouldBe newToken
    }

  }

  def runTest(
    test: (
      (
        TestAccessTokenProvider,
        AccessTokenProvider[Future],
        TestTimeProvider
      )
    ) => Future[Assertion]
  ): Future[Assertion] = {
    val delegate = TestAccessTokenProvider.instance()
    val timeProvider = TestTimeProvider.instance(someTime)
    val cache = MonixFutureCache[Option[Scope], TokenWithExpirationTime](timeProvider)
    val cacheProvider = FutureCachingAccessTokenProvider(delegate, cache, timeProvider)

    test((delegate, cacheProvider, timeProvider))
  }

}
