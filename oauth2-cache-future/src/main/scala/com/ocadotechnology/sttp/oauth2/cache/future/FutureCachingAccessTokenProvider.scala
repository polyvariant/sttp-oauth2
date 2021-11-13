package com.ocadotechnology.sttp.oauth2.cache.future

import cats.data.OptionT
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.cache.ExpiringCache
import com.ocadotechnology.sttp.oauth2.cache.future.FutureCachingAccessTokenProvider.TokenWithExpirationTime
import com.ocadotechnology.sttp.oauth2.common.Scope
import com.ocadotechnology.sttp.oauth2.AccessTokenProvider
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken
import com.ocadotechnology.sttp.oauth2.Secret

import java.time.Instant
import scala.concurrent.duration.Duration
import monix.execution.AsyncSemaphore
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

final class FutureCachingAccessTokenProvider(
  delegate: AccessTokenProvider[Future],
  tokenCache: ExpiringCache[Future, Scope, TokenWithExpirationTime],
  timeProvider: TimeProvider
)(
  implicit ec: ExecutionContext
) extends AccessTokenProvider[Future] {

  val semaphore: AsyncSemaphore = AsyncSemaphore(provisioned = 1)

  override def requestToken(scope: Scope): Future[ClientCredentialsToken.AccessTokenResponse] =
    getFromCache(scope)
      .getOrElseF(semaphore.withPermit(() => acquireToken(scope))) // semaphore prevents concurrent token fetch from external service

  private def acquireToken(scope: Scope) =
    getFromCache(scope) // duplicate cache check, to verify if any other thread filled the cache during wait for semaphore permit
      .getOrElseF(fetchAndSaveToken(scope))

  private def getFromCache(scope: Scope) =
    OptionT(tokenCache.get(scope)).map(_.toAccessTokenResponse(timeProvider.currentInstant()))

  private def fetchAndSaveToken(scope: Scope) =
    for {
      token <- delegate.requestToken(scope)
      tokenWithExpiry = calculateExpiryInstant(token)
      _     <- tokenCache.put(scope, tokenWithExpiry, tokenWithExpiry.expirationTime)
    } yield token

  private def calculateExpiryInstant(response: ClientCredentialsToken.AccessTokenResponse): TokenWithExpirationTime =
    TokenWithExpirationTime.from(response, timeProvider.currentInstant())

}

object FutureCachingAccessTokenProvider {

  def apply(
    delegate: AccessTokenProvider[Future],
    tokenCache: ExpiringCache[Future, Scope, TokenWithExpirationTime],
    timeProvider: TimeProvider = TimeProvider.default
  )(
    implicit ec: ExecutionContext
  ): FutureCachingAccessTokenProvider = new FutureCachingAccessTokenProvider(delegate, tokenCache, timeProvider)

  def monixCacheInstance(
    delegate: AccessTokenProvider[Future],
    timeProvider: TimeProvider = TimeProvider.default
  )(
    implicit ec: ExecutionContext
  ): FutureCachingAccessTokenProvider = FutureCachingAccessTokenProvider(delegate, MonixFutureCache(), timeProvider)

  final case class TokenWithExpirationTime(
    accessToken: Secret[String],
    domain: Option[String],
    expirationTime: Instant,
    scope: Scope
  ) {

    def toAccessTokenResponse(now: Instant): ClientCredentialsToken.AccessTokenResponse = {
      val newExpiresIn = Duration.fromNanos(java.time.Duration.between(now, expirationTime).toNanos)
      ClientCredentialsToken.AccessTokenResponse(accessToken, domain, newExpiresIn, scope)
    }

  }

  object TokenWithExpirationTime {

    def from(token: ClientCredentialsToken.AccessTokenResponse, now: Instant): TokenWithExpirationTime = {
      val expirationTime = now.plusNanos(token.expiresIn.toNanos)
      TokenWithExpirationTime(token.accessToken, token.domain, expirationTime, token.scope)
    }

  }

}
