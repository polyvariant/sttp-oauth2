package com.ocadotechnology.sttp.oauth2

import cats.Functor
import common._
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client._
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
    sttpBackend: SttpBackend[F, Nothing, NothingT]
  ): F[OAuth2Token.Response] = {
    implicit val backend: SttpBackend[F, Nothing, NothingT] = sttpBackend
    basicRequest
      .post(tokenUri)
      .body(requestTokenParams(clientId, user, clientSecret, scope))
      .response(OAuth2Token.response)
      .send()
      .map(_.body)
  }

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
