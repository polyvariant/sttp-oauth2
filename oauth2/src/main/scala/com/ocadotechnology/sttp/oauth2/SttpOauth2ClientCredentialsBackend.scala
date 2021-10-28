package com.ocadotechnology.sttp.oauth2

import cats.Monad
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.common.Scope
import eu.timepit.refined.types.string.NonEmptyString
import sttp.capabilities.Effect
import sttp.client3._
import sttp.model.Uri

/** SttpBackend, that adds auth bearer headers to every request.
  */
final class SttpOauth2ClientCredentialsBackend[F[_]: Monad, P] private (
  delegate: SttpBackend[F, P],
  accessTokenProvider: AccessTokenProvider[F],
  scope: common.Scope
) extends DelegateSttpBackend(delegate) {

  override def send[T, R >: P with Effect[F]](request: Request[T, R]): F[Response[T]] = for {
    token    <- accessTokenProvider.requestToken(scope)
    response <- delegate.send(request.auth.bearer(token.accessToken.value))
  } yield response

}

object SttpOauth2ClientCredentialsBackend {

  def apply[F[_]: Monad, P](
    tokenUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[F, P]
  ): SttpOauth2ClientCredentialsBackend[F, P] = {
    val accessTokenProvider = AccessTokenProvider.instance(tokenUrl, clientId, clientSecret)
    SttpOauth2ClientCredentialsBackend(accessTokenProvider)(scope)
  }

  def apply[F[_]: Monad, P](
    accessTokenProvider: AccessTokenProvider[F]
  )(
    scope: Scope
  )(
    implicit backend: SttpBackend[F, P]
  ) =
    new SttpOauth2ClientCredentialsBackend[F, P](backend, accessTokenProvider, scope)

}
