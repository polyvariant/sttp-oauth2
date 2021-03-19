package com.ocadotechnology.sttp.oauth2.backend

import cats.Monad
import cats.data.OptionT
import cats.effect.Clock
import cats.effect.Concurrent
import cats.effect.concurrent.Semaphore
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.ClientCredentialsProvider
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.backend.SttpOauth2ClientCredentialsCatsBackend.TokenWithExpiryInstant
import com.ocadotechnology.sttp.oauth2.common.Scope
import eu.timepit.refined.types.string.NonEmptyString
import sttp.capabilities.Effect
import sttp.client3._
import sttp.model.Uri

import java.time.Instant

final class SttpOauth2ClientCredentialsCatsBackend[F[_]: Monad: Clock, P] private (
  delegate: SttpBackend[F, P],
  fetchTokenAction: F[AccessTokenResponse],
  cache: Cache[F, TokenWithExpiryInstant],
  semaphore: Semaphore[F]
) extends DelegateSttpBackend(delegate) {

  override def send[T, R >: P with Effect[F]](request: Request[T, R]): F[Response[T]] = for {
    token    <- semaphore.withPermit(resolveToken)
    response <- delegate.send(request.auth.bearer(token.value))
  } yield response

  private val resolveToken: F[Secret[String]] =
    OptionT(cache.get)
      .product(OptionT.liftF(Clock[F].instantNow))
      .filter { case (TokenWithExpiryInstant(_, expiryInstant), currentInstant) => currentInstant.isBefore(expiryInstant) }
      .map(_._1)
      .getOrElseF(fetchAndSaveToken)
      .map(_.token)

  private def fetchAndSaveToken: F[TokenWithExpiryInstant] =
    fetchTokenAction.flatMap(calculateExpiryInstant).flatTap(cache.set)

  private def calculateExpiryInstant(response: AccessTokenResponse): F[TokenWithExpiryInstant] =
    Clock[F].instantNow.map(_ plusMillis response.expiresIn.toMillis).map(TokenWithExpiryInstant(response.accessToken, _))

}

object SttpOauth2ClientCredentialsCatsBackend {
  final case class TokenWithExpiryInstant(token: Secret[String], expiryInstant: Instant)

  def apply[F[_]: Concurrent: Clock, P](
    tokenUrl: Uri,
    tokenIntrospectionUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[F, P]
  ): F[SttpOauth2ClientCredentialsCatsBackend[F, P]] = {
    val clientCredentialsProvider = ClientCredentialsProvider.instance(tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)
    usingClientCredentialsProvider(clientCredentialsProvider)(scope)
  }

  /** Keep in mind that the given implicit `backend` may be different than this one used by `clientCredentialsProvider`
    */
  def usingClientCredentialsProvider[F[_]: Concurrent: Clock, P](
    clientCredentialsProvider: ClientCredentialsProvider[F]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[F, P]
  ): F[SttpOauth2ClientCredentialsCatsBackend[F, P]] =
    CatsRefCache[F, TokenWithExpiryInstant].flatMap(usingClientCredentialsProviderAndCache(clientCredentialsProvider, _)(scope))

  def usingCache[F[_]: Concurrent: Clock, P](
    cache: Cache[F, TokenWithExpiryInstant]
  )(
    tokenUrl: Uri,
    tokenIntrospectionUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[F, P]
  ): F[SttpOauth2ClientCredentialsCatsBackend[F, P]] = {
    val clientCredentialsProvider = ClientCredentialsProvider.instance(tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)
    usingClientCredentialsProviderAndCache(clientCredentialsProvider, cache)(scope)
  }

  /** Keep in mind that the given implicit `backend` may be different than this one used by `clientCredentialsProvider`
    */
  def usingClientCredentialsProviderAndCache[F[_]: Concurrent: Clock, P](
    clientCredentialsProvider: ClientCredentialsProvider[F],
    cache: Cache[F, TokenWithExpiryInstant]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[F, P]
  ): F[SttpOauth2ClientCredentialsCatsBackend[F, P]] =
    usingFetchTokenActionAndCache(clientCredentialsProvider.requestToken(scope), cache)

  /** Keep in mind that the given implicit `backend` may be different than this one used by `fetchTokenAction`
    */
  def usingFetchTokenActionAndCache[F[_]: Concurrent: Clock, P](
    fetchTokenAction: F[AccessTokenResponse],
    cache: Cache[F, TokenWithExpiryInstant]
  )(
    implicit backend: SttpBackend[F, P]
  ): F[SttpOauth2ClientCredentialsCatsBackend[F, P]] =
    Semaphore(n = 1).map(new SttpOauth2ClientCredentialsCatsBackend(backend, fetchTokenAction, cache, _))

}
