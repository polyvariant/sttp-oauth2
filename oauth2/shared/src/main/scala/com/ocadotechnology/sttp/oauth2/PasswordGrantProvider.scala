package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.PasswordGrant.User
import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client4.GenericBackend
import sttp.model.Uri
import sttp.monad.MonadError
import sttp.monad.syntax._

trait PasswordGrantProvider[F[_]] {
  def requestToken(user: User, scope: Scope): F[ExtendedOAuth2TokenResponse]
}

object PasswordGrantProvider {

  def apply[F[_]](implicit ev: PasswordGrantProvider[F]): PasswordGrantProvider[F] = ev

  def apply[F[_]](
    tokenUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    backend: GenericBackend[F, Any]
  )(
    implicit decoder: JsonDecoder[ExtendedOAuth2TokenResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): PasswordGrantProvider[F] = { (user: User, scope: Scope) =>
    implicit val F: MonadError[F] = backend.monad
    PasswordGrant
      .requestToken(tokenUrl, user, clientId, clientSecret, scope)(backend)
      .map(_.leftMap(OAuth2Exception.apply))
      .flatMap(_.fold(F.error, F.unit))
  }

}
