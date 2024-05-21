package org.polyvariant.sttp.oauth2.cache.zio

import org.polyvariant.sttp.oauth2.AccessTokenProvider
import org.polyvariant.sttp.oauth2.ClientCredentialsToken
import org.polyvariant.sttp.oauth2.Secret
import org.polyvariant.sttp.oauth2.cache.ExpiringCache
import org.polyvariant.sttp.oauth2.cache.zio.CachingAccessTokenProvider.TokenWithExpirationTime
import org.polyvariant.sttp.oauth2.common.Scope
import zio.Clock
import zio.Semaphore
import zio._

import java.time.Instant
import scala.concurrent.duration.Duration

final class CachingAccessTokenProvider[R](
  delegate: AccessTokenProvider[RIO[R, _]],
  semaphore: Semaphore,
  tokenCache: ExpiringCache[RIO[R, _], Option[Scope], TokenWithExpirationTime]
) extends AccessTokenProvider[RIO[R, _]] {

  override def requestToken(scope: Option[Scope]): RIO[R, ClientCredentialsToken.AccessTokenResponse] =
    getFromCache(scope).flatMap {
      case Some(value) => ZIO.succeed(value)
      case None        => semaphore.withPermit(acquireToken(scope))
    }

  private def acquireToken(scope: Option[Scope]): ZIO[R, Throwable, ClientCredentialsToken.AccessTokenResponse] =
    getFromCache(scope).flatMap {
      case Some(value) => ZIO.succeed(value)
      case None        => fetchAndSaveToken(scope)
    }

  private def getFromCache(scope: Option[Scope]) =
    tokenCache.get(scope).flatMap { entry =>
      Clock.instant.map { now =>
        entry match {
          case Some(value) => Some(value.toAccessTokenResponse(now))
          case None        => None
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

  def apply[R](
    delegate: AccessTokenProvider[RIO[R, _]],
    tokenCache: ExpiringCache[RIO[R, _], Option[Scope], TokenWithExpirationTime]
  ): RIO[R, CachingAccessTokenProvider[R]] = Semaphore.make(permits = 1).map(new CachingAccessTokenProvider(delegate, _, tokenCache))

  def refCacheInstance(delegate: AccessTokenProvider[Task]): Task[CachingAccessTokenProvider[Any]] =
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
