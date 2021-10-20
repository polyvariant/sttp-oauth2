package com.ocadotechnology.sttp.oauth2.backend

import cats.implicits._
import com.ocadotechnology.sttp.oauth2.AccessTokenProvider
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.backend.SttpOauth2ClientCredentialsFutureBackend.TokenWithExpiryInstant
import com.ocadotechnology.sttp.oauth2.common.Scope
import eu.timepit.refined.types.string.NonEmptyString
import monix.execution.AsyncSemaphore
import sttp.capabilities.Effect
import sttp.client3._
import sttp.model.Uri

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

final class SttpOauth2ClientCredentialsFutureBackend[P] private (
  delegate: SttpBackend[Future, P],
  fetchTokenAction: () => Future[AccessTokenResponse],
  cache: Cache[Future, TokenWithExpiryInstant]
)(
  implicit ec: ExecutionContext,
  timeProvider: TimeProvider
) extends DelegateSttpBackend(delegate) {
  val semaphore: AsyncSemaphore = AsyncSemaphore(provisioned = 1)

  override def send[T, R >: P with Effect[Future]](request: Request[T, R]): Future[Response[T]] = for {
    token    <- semaphore.withPermit(() => resolveToken())
    response <- delegate.send(request.auth.bearer(token.value))
  } yield response

  private def resolveToken(): Future[Secret[String]] = for {
    cachedToken            <- cache.get
    currentInstant = timeProvider.currentInstant()
    tokenWithExpiryInstant <- cachedToken.filter(t => currentInstant.isBefore(t.expiryInstant)).fold(fetchAndSaveToken())(Future.successful)
  } yield tokenWithExpiryInstant.token

  private def fetchAndSaveToken(): Future[TokenWithExpiryInstant] =
    fetchTokenAction().map(calculateExpiryInstant).flatTap(cache.set)

  private def calculateExpiryInstant(response: AccessTokenResponse): TokenWithExpiryInstant =
    TokenWithExpiryInstant(response.accessToken, timeProvider.currentInstant() plusMillis response.expiresIn.toMillis)
}

object SttpOauth2ClientCredentialsFutureBackend {
  final case class TokenWithExpiryInstant(token: Secret[String], expiryInstant: Instant)

  def apply[P](
    tokenUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[Future, P],
    ec: ExecutionContext
  ): SttpOauth2ClientCredentialsFutureBackend[P] = {
    implicit val timeProvider: TimeProvider = TimeProvider.default
    val accessTokenProvider = AccessTokenProvider.instance(tokenUrl, clientId, clientSecret)
    usingAccessTokenProvider(accessTokenProvider)(scope)
  }

  /** Keep in mind that the given implicit `backend` may be different than this one used by `accessTokenProvider`
    */
  def usingAccessTokenProvider[P](
    accessTokenProvider: AccessTokenProvider[Future]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[Future, P],
    ec: ExecutionContext,
    timeProvider: TimeProvider
  ): SttpOauth2ClientCredentialsFutureBackend[P] =
    usingAccessTokenProviderAndCache(accessTokenProvider, new MonixFutureCache)(scope)

  def usingCache[P](
    cache: Cache[Future, TokenWithExpiryInstant]
  )(
    tokenUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[Future, P],
    ec: ExecutionContext,
    timeProvider: TimeProvider
  ): SttpOauth2ClientCredentialsFutureBackend[P] = {
    val accessTokenProvider = AccessTokenProvider.instance(tokenUrl, clientId, clientSecret)
    usingAccessTokenProviderAndCache(accessTokenProvider, cache)(scope)
  }

  /** Keep in mind that the given implicit `backend` may be different than this one used by `accessTokenProvider`
    */
  def usingAccessTokenProviderAndCache[P](
    accessTokenProvider: AccessTokenProvider[Future],
    cache: Cache[Future, TokenWithExpiryInstant]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[Future, P],
    ec: ExecutionContext,
    timeProvider: TimeProvider
  ): SttpOauth2ClientCredentialsFutureBackend[P] =
    usingFetchTokenActionAndCache(() => accessTokenProvider.requestToken(scope), cache)

  /** Keep in mind that the given implicit `backend` may be different than this one used by `fetchTokenAction`
    */
  def usingFetchTokenActionAndCache[P](
    fetchTokenAction: () => Future[AccessTokenResponse],
    cache: Cache[Future, TokenWithExpiryInstant]
  )(
    implicit backend: SttpBackend[Future, P],
    ec: ExecutionContext,
    timeProvider: TimeProvider
  ): SttpOauth2ClientCredentialsFutureBackend[P] =
    new SttpOauth2ClientCredentialsFutureBackend(backend, fetchTokenAction, cache)

}
