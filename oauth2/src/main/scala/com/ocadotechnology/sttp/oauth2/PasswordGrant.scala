package com.ocadotechnology.sttp.oauth2

import cats.Functor
import common._
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client3._
import sttp.model.Uri
import cats.syntax.all._

object PasswordGrant {

  final case class User(name: NonEmptyString, password: Secret[NonEmptyString])

  object User {

    // TODO think about error type
    def of(name: String, password: String): Either[String, User] =
      for {
        refinedName     <- NonEmptyString.from(name)
        refinedPassword <- NonEmptyString.from(password)
      } yield User(refinedName, Secret(refinedPassword))

  }

  def requestToken[F[_]: Functor](
    tokenUri: Uri,
    user: User,
    clientId: NonEmptyString,
    clientSecret: Secret[String],
    scope: Scope
  )(
    backend: SttpBackend[F, Any]
  ): F[OAuth2Token.Response] =
    backend
      .send {
        basicRequest
          .post(tokenUri)
          .body(requestTokenParams(clientId, user, clientSecret, scope))
          .response(OAuth2Token.response)
      }
      .map(_.body)

  private def requestTokenParams(clientId: NonEmptyString, user: User, clientSecret: Secret[String], scope: Scope) =
    Map(
      "grant_type" -> "password",
      "username" -> user.name.value,
      "password" -> user.password.value.value,
      "client_id" -> clientId.value,
      "client_secret" -> clientSecret.value,
      "scope" -> scope.value
    )

}
