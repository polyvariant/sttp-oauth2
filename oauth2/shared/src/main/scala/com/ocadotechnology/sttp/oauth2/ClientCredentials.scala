package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client3.SttpBackend
import sttp.client3.basicRequest
import sttp.model.Uri
import sttp.monad.MonadError
import sttp.monad.syntax._

object ClientCredentials {

  /** Requests token from OAuth2 provider `tokenUri` using `clientId`, `clientSecret`, requested `scope` and `client_credentials` grant
    * type. Request is performed with provided `backend`.
    *
    * All errors are mapped to [[common.Error]] ADT.
    */
  def requestToken[F[_]](
    tokenUri: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String],
    scope: Option[Scope]
  )(
    backend: SttpBackend[F, Any]
  )(
    implicit decoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): F[ClientCredentialsToken.Response] = {
    implicit val F: MonadError[F] = backend.responseMonad
    backend
      .send {
        basicRequest
          .post(tokenUri)
          .body(requestTokenParams(clientId, clientSecret, scope))
          .response(ClientCredentialsToken.response)
      }
      .map(_.body)
  }

  private def requestTokenParams(clientId: NonEmptyString, clientSecret: Secret[String], scope: Option[Scope]) =
    Map(
      "grant_type" -> "client_credentials",
      "client_id" -> clientId.value,
      "client_secret" -> clientSecret.value
    ) ++ scope.map(s => Map("scope" -> s.value)).getOrElse(Map.empty)

  /** Introspects provided `token` in OAuth2 provider `tokenIntrospectionUri`, using `clientId` and `clientSecret`. Request is performed
    * with provided `backend`.
    *
    * Errors are mapped to [[common.Error]] ADT.
    */
  def introspectToken[F[_]](
    tokenIntrospectionUri: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String],
    token: Secret[String]
  )(
    backend: SttpBackend[F, Any]
  )(
    implicit decoder: JsonDecoder[TokenIntrospectionResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): F[Introspection.Response] = {
    implicit val F: MonadError[F] = backend.responseMonad
    backend
      .send {
        basicRequest
          .post(tokenIntrospectionUri)
          .body(requestTokenIntrospectionParams(clientId, clientSecret, token))
          .response(Introspection.response)
      }
      .map(_.body)
  }

  private def requestTokenIntrospectionParams(clientId: NonEmptyString, clientSecret: Secret[String], token: Secret[String]) =
    Map(
      "client_id" -> clientId.value,
      "client_secret" -> clientSecret.value,
      "token" -> token.value
    )

}
