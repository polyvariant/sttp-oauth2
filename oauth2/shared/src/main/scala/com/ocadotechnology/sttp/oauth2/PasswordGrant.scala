package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client3._
import sttp.model.Uri
import sttp.monad.MonadError
import sttp.monad.syntax._

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

  def requestToken[F[_]](
    tokenUri: Uri,
    user: User,
    clientId: NonEmptyString,
    clientSecret: Secret[String],
    scope: Scope
  )(
    backend: SttpBackend[F, Any]
  )(implicit decoder: JsonDecoder[ExtendedOAuth2TokenResponse], oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]): F[OAuth2Token.Response] = {
    implicit val F: MonadError[F] = backend.responseMonad
    backend
      .send {
        basicRequest
          .post(tokenUri)
          .body(requestTokenParams(clientId, user, clientSecret, scope))
          .response(OAuth2Token.response)
      }
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
