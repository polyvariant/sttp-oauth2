package org.polyvariant.sttp.oauth2.cache.ce2

import cats.Monad
import cats.data.OptionT
import cats.effect.Clock
import cats.effect.Concurrent
import cats.effect.concurrent.Semaphore
import cats.syntax.all._
import org.polyvariant.sttp.oauth2.cache.ExpiringCache
import org.polyvariant.sttp.oauth2.cache.ce2.CachingAccessTokenProvider.TokenWithExpirationTime
import org.polyvariant.sttp.oauth2.common.Scope
import org.polyvariant.sttp.oauth2.AccessTokenProvider
import org.polyvariant.sttp.oauth2.ClientCredentialsToken
import org.polyvariant.sttp.oauth2.Secret

import java.time.Instant
import scala.concurrent.duration.Duration

final class CachingAccessTokenProvider[F[_]: Monad: Clock](
  delegate: AccessTokenProvider[F],
  semaphore: Semaphore[F],
  tokenCache: ExpiringCache[F, Option[Scope], TokenWithExpirationTime]
) extends AccessTokenProvider[F] {

  override def requestToken(scope: Option[Scope]): F[ClientCredentialsToken.AccessTokenResponse] =
    getFromCache(scope)
      .getOrElseF(semaphore.withPermit(acquireToken(scope))) // semaphore prevents concurrent token fetch from external service

  private def acquireToken(scope: Option[Scope]) =
    getFromCache(scope) // duplicate cache check, to verify if any other thread filled the cache during wait for semaphore permit
      .getOrElseF(fetchAndSaveToken(scope))

  private def getFromCache(scope: Option[Scope]) =
    (OptionT(tokenCache.get(scope)), OptionT.liftF(Clock[F].instantNow))
      .mapN(_.toAccessTokenResponse(_))

  private def fetchAndSaveToken(scope: Option[Scope]) =
    for {
      token           <- delegate.requestToken(scope)
      tokenWithExpiry <- calculateExpiryInstant(token)
      _               <- tokenCache.put(scope, tokenWithExpiry, tokenWithExpiry.expirationTime)
    } yield token

  private def calculateExpiryInstant(response: ClientCredentialsToken.AccessTokenResponse): F[TokenWithExpirationTime] =
    Clock[F].instantNow.map(TokenWithExpirationTime.from(response, _))

}

object CachingAccessTokenProvider {

  def apply[F[_]: Concurrent: Clock](
    delegate: AccessTokenProvider[F],
    tokenCache: ExpiringCache[F, Option[Scope], TokenWithExpirationTime]
  ): F[CachingAccessTokenProvider[F]] = Semaphore[F](n = 1).map(new CachingAccessTokenProvider[F](delegate, _, tokenCache))

  def refCacheInstance[F[_]: Concurrent: Clock](delegate: AccessTokenProvider[F]): F[CachingAccessTokenProvider[F]] =
    CatsRefExpiringCache[F, Option[Scope], TokenWithExpirationTime].flatMap(CachingAccessTokenProvider(delegate, _))

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
