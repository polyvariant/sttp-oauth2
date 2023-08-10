package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.common.Scope
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import eu.timepit.refined.types.string.NonEmptyString
import sttp.capabilities.Effect
import sttp.client4._
import sttp.client4.wrappers.DelegateBackend
import sttp.model.Uri
import sttp.monad.MonadError
import sttp.monad.syntax._

/** SttpBackend, that adds auth bearer headers to every request.
  */
final class SttpOauth2ClientCredentialsBackend[F[_], P] private (
  delegate: GenericBackend[F, P],
  accessTokenProvider: AccessTokenProvider[F],
  scope: Option[common.Scope]
) extends DelegateBackend(delegate) {
  implicit val F: MonadError[F] = delegate.monad

  override def send[T](request: GenericRequest[T, P with Effect[F]]): F[Response[T]] = for {
    token    <- accessTokenProvider.requestToken(scope)
    response <- delegate.send(request.auth.bearer(token.accessToken.value))
  } yield response

}

object SttpOauth2ClientCredentialsBackend {

  def apply[F[_], P](
    tokenUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    scope: Option[Scope]
  )(
    backend: GenericBackend[F, P]
  )(
    implicit decoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): SttpOauth2ClientCredentialsBackend[F, P] = {
    val accessTokenProvider = AccessTokenProvider[F](tokenUrl, clientId, clientSecret)(backend)
    SttpOauth2ClientCredentialsBackend(accessTokenProvider)(scope)(backend)
  }

  def apply[F[_], P](
    accessTokenProvider: AccessTokenProvider[F]
  )(
    scope: Option[Scope]
  )(
    backend: GenericBackend[F, P]
  ) =
    new SttpOauth2ClientCredentialsBackend[F, P](backend, accessTokenProvider, scope)

}
