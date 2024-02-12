package com.ocadotechnology.sttp.oauth2.cache.zio

import com.ocadotechnology.sttp.oauth2.{AccessTokenProvider, ClientCredentialsToken, Secret}
import com.ocadotechnology.sttp.oauth2.cache.ExpiringCache
import com.ocadotechnology.sttp.oauth2.cache.zio.CachingAccessTokenProvider.TokenWithExpirationTime
import com.ocadotechnology.sttp.oauth2.common.Scope
import zio.{Clock, Semaphore, Task, ZIO}

import java.time.Instant
import scala.concurrent.duration.Duration

final class CachingAccessTokenProvider(
  delegate: AccessTokenProvider[Task],
  semaphore: Semaphore,
  tokenCache: ExpiringCache[Task, Option[Scope], TokenWithExpirationTime]
) extends AccessTokenProvider[Task] {

  override def requestToken(scope: Option[Scope]): Task[ClientCredentialsToken.AccessTokenResponse] =
    getFromCache(scope).flatMap {
      case Some(value) => ZIO.succeed(value)
      case None => semaphore.withPermit(acquireToken(scope))
    }

  private def acquireToken(scope: Option[Scope]): ZIO[Any, Throwable, ClientCredentialsToken.AccessTokenResponse] =
    getFromCache(scope).flatMap {
      case Some(value) => ZIO.succeed(value)
      case None => fetchAndSaveToken(scope)
    }

  private def getFromCache(scope: Option[Scope]) = {
    tokenCache.get(scope).flatMap { entry =>
      Clock.instant.map { now =>
        entry match {
          case Some(value) => Some(value.toAccessTokenResponse(now))
          case None => None
        }
      }
    }
  }

  private def fetchAndSaveToken(scope: Option[Scope]) =
    for {
      token           <- delegate.requestToken(scope)
      tokenWithExpiry <- calculateExpiryInstant(token)
      _               <- tokenCache.put(scope, tokenWithExpiry, tokenWithExpiry.expirationTime)
    } yield token

  private def calculateExpiryInstant(response: ClientCredentialsToken.AccessTokenResponse) =
    Clock.instant.map(TokenWithExpirationTime.from(response, _))

}

object CachingAccessTokenProvider {

  def apply(
    delegate: AccessTokenProvider[Task],
    tokenCache: ExpiringCache[Task, Option[Scope], TokenWithExpirationTime]
  ): Task[CachingAccessTokenProvider] = Semaphore.make(permits = 1).map(new CachingAccessTokenProvider(delegate, _, tokenCache))

  def refCacheInstance(delegate: AccessTokenProvider[Task]): Task[CachingAccessTokenProvider] =
    ZioRefExpiringCache[Option[Scope], TokenWithExpirationTime].flatMap(CachingAccessTokenProvider(delegate, _))

  final case class TokenWithExpirationTime(
    accessToken: Secret[String],
    domain: Option[String],
    expirationTime: Instant,
    scope: Option[Scope]
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
