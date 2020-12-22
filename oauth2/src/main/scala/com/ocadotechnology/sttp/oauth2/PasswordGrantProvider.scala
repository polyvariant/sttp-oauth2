package com.ocadotechnology.sttp.oauth2

import cats.MonadError
import common._
import com.ocadotechnology.sttp.oauth2.PasswordGrant.User
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client.NothingT
import sttp.client.SttpBackend
import sttp.model.Uri
import cats.syntax.all._

trait PasswordGrantProvider[F[_]] {
  def requestToken(user: User, scope: Scope): F[Oauth2TokenResponse]
}

object PasswordGrantProvider {

  def apply[F[_]](implicit ev: PasswordGrantProvider[F]): PasswordGrantProvider[F] = ev

  def instance[F[_]: MonadError[*[_], Throwable]](
    tokenUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    implicit sttpBackend: SttpBackend[F, Nothing, NothingT]
  ): PasswordGrantProvider[F] = { (user: User, scope: Scope) =>
    PasswordGrant.requestToken(tokenUrl, user, clientId, clientSecret, scope)(sttpBackend).map(_.leftMap(OAuth2Exception)).rethrow
  }

}
