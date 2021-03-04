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
import sttp.client3.DelegateSttpBackend
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.SttpBackend
import sttp.model.Uri

import java.time.Instant
import scala.concurrent.duration.MILLISECONDS

class SttpOauth2ClientCredentialsCatsBackend[F[_]: Monad: Clock, P] private (
  delegate: SttpBackend[F, P],
  clientCredentialsProvider: ClientCredentialsProvider[F],
  cache: Cache[F, TokenWithExpiryInstant],
  semaphore: Semaphore[F],
  val scope: Scope
) extends DelegateSttpBackend(delegate) {

  override def send[T, R >: P with Effect[F]](request: Request[T, R]): F[Response[T]] = for {
    token    <- semaphore.withPermit(resolveToken)
    response <- delegate.send(request.auth.bearer(token.value))
  } yield response

  private val resolveToken: F[Secret[String]] =
    OptionT(cache.get)
      .product(OptionT.liftF(getCurrentInstant))
      .filter { case (TokenWithExpiryInstant(_, expiryInstant), currentInstant) => currentInstant isBefore expiryInstant }
      .map(_._1)
      .getOrElseF(requestAndSaveToken)
      .map(_.token)

  private def requestAndSaveToken: F[TokenWithExpiryInstant] =
    clientCredentialsProvider.requestToken(scope).flatMap(calculateExpiryTime).flatTap(cache.set)

  private def calculateExpiryTime(response: AccessTokenResponse): F[TokenWithExpiryInstant] =
    getCurrentInstant.map(_ plusMillis response.expiresIn.toMillis).map(TokenWithExpiryInstant(response.accessToken, _))

  private def getCurrentInstant: F[Instant] = Clock[F].realTime(MILLISECONDS).map(Instant.ofEpochMilli)

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

  /** Keep in mind that the given implicit `backend` may be different than this one used in `clientCredentialsProvider`
    */
  def usingClientCredentialsProvider[F[_]: Concurrent: Clock, P](
    clientCredentialsProvider: ClientCredentialsProvider[F]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[F, P]
  ): F[SttpOauth2ClientCredentialsCatsBackend[F, P]] =
    Cache.refCache[F, TokenWithExpiryInstant].flatMap(usingClientCredentialsProviderAndCache(clientCredentialsProvider, _)(scope))

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

  /** Keep in mind that the given implicit `backend` may be different than this one used in `clientCredentialsProvider`
    */
  def usingClientCredentialsProviderAndCache[F[_]: Concurrent: Clock, P](
    clientCredentialsProvider: ClientCredentialsProvider[F],
    cache: Cache[F, TokenWithExpiryInstant]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[F, P]
  ): F[SttpOauth2ClientCredentialsCatsBackend[F, P]] =
    Semaphore(n = 1).map(new SttpOauth2ClientCredentialsCatsBackend(backend, clientCredentialsProvider, cache, _, scope))

}
