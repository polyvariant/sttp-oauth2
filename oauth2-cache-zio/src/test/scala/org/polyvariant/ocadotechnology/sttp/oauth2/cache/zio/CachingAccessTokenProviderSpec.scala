package org.polyvariant.sttp.oauth2.cache.zio

import org.polyvariant.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import org.polyvariant.sttp.oauth2.Secret
import org.polyvariant.sttp.oauth2.cache.zio.CachingAccessTokenProvider.TokenWithExpirationTime
import org.polyvariant.sttp.oauth2.common.Scope
import zio.test._
import zio.Ref
import zio.{Duration => ZDuration}

import scala.concurrent.duration._

object CachingAccessTokenProviderSpec extends ZIOSpecDefault {

  private val testScope: Option[Scope] = Scope.of("test-scope")
  private val token = AccessTokenResponse(Secret("secret"), None, 10.seconds, testScope)
  private val newToken = AccessTokenResponse(Secret("secret2"), None, 20.seconds, testScope)

  def spec = suite("CachingAccessTokenProvider")(
    test("delegate token retrieval on first call") {
      prepareTest.flatMap { case (delegate, cachingProvider) =>
        for {
          _      <- delegate.setToken(testScope, token)
          result <- cachingProvider.requestToken(testScope)
        } yield assert(result)(Assertion.equalTo(token))
      }
    },
    test("decrease expiresIn in second read") {
      prepareTest.flatMap { case (delegate, cachingProvider) =>
        for {
          _      <- delegate.setToken(testScope, token)
          _      <- cachingProvider.requestToken(testScope)
          _      <- TestClock.adjust(ZDuration.fromScala(3.seconds))
          result <- cachingProvider.requestToken(testScope)
        } yield assert(result)(Assertion.equalTo(token.copy(expiresIn = 7.seconds)))
      }
    },
    test("not refresh token before expiration") {
      prepareTest.flatMap { case (delegate, cachingProvider) =>
        for {
          _      <- delegate.setToken(testScope, token)
          _      <- cachingProvider.requestToken(testScope)
          _      <- delegate.setToken(testScope, newToken)
          _      <- TestClock.adjust(ZDuration.fromScala(10.seconds - 1.milliseconds))
          result <- cachingProvider.requestToken(testScope)
        } yield assert(result)(Assertion.equalTo(token.copy(expiresIn = 1.milliseconds)))
      }
    },
    test("ask for token again after expiration") {
      prepareTest.flatMap { case (delegate, cachingProvider) =>
        for {
          _      <- delegate.setToken(testScope, token)
          _      <- cachingProvider.requestToken(testScope)
          _      <- delegate.setToken(testScope, newToken)
          _      <- TestClock.adjust(ZDuration.fromScala(11.seconds))
          result <- cachingProvider.requestToken(testScope)
        } yield assert(result)(Assertion.equalTo(newToken))
      }
    }
  )

  private def prepareTest =
    for {
      state           <- Ref.make[TestAccessTokenProvider.State](TestAccessTokenProvider.State.empty)
      delegate = TestAccessTokenProvider(state)
      cache           <- ZioRefExpiringCache[Option[Scope], TokenWithExpirationTime]
      cachingProvider <- CachingAccessTokenProvider(delegate, cache)
    } yield (delegate, cachingProvider)

}
