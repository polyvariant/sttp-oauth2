package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client4.GenericBackend
import sttp.model.Uri

/** Tagless Final algebra for ClientCredentials token requests and verification.
  */
trait ClientCredentialsProvider[F[_]] extends AccessTokenProvider[F] with TokenIntrospection[F]

object ClientCredentialsProvider {

  /** Create instance of auth provider with sttp backend.
    *
    * `clientId`, `clientSecret`, `applicationScope` are parameters of your application.
    */
  def apply[F[_]](
    tokenUrl: Uri,
    tokenIntrospectionUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    backend: GenericBackend[F, Any]
  )(
    implicit accessTokenResponseDecoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse],
    tokenIntrospectionResponseDecoder: JsonDecoder[TokenIntrospectionResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): ClientCredentialsProvider[F] =
    ClientCredentialsProvider[F](
      AccessTokenProvider[F](tokenUrl, clientId, clientSecret)(backend),
      TokenIntrospection[F](tokenIntrospectionUrl, clientId, clientSecret)(backend)
    )

  def apply[F[_]](accessTokenProvider: AccessTokenProvider[F], tokenIntrospection: TokenIntrospection[F]): ClientCredentialsProvider[F] =
    new ClientCredentialsProvider[F] {
      override def requestToken(scope: Option[Scope]): F[ClientCredentialsToken.AccessTokenResponse] =
        accessTokenProvider.requestToken(scope)

      override def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse] =
        tokenIntrospection.introspect(token)

    }

}
