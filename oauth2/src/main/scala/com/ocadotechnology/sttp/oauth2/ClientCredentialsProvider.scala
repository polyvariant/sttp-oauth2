package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client3.SttpBackend
import sttp.model.Uri

/** Tagless Final algebra for ClientCredentials token requests and verification.
  */
trait ClientCredentialsProvider[F[_]] extends AccessTokenProvider[F] with TokenIntrospection[F]

object ClientCredentialsProvider {

  /** Create instance of auth provider with sttp backend.
    *
    * `clientId`, `clientSecret`, `applicationScope` are parameters of your application.
    */
  def instance[F[_]](
    tokenUrl: Uri,
    tokenIntrospectionUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    implicit backend: SttpBackend[F, Any]
  ): ClientCredentialsProvider[F] =
    from[F](
      AccessTokenProvider.instance[F](tokenUrl, clientId, clientSecret),
      TokenIntrospection.instance[F](tokenIntrospectionUrl, clientId, clientSecret)
    )

  def from[F[_]](accessTokenProvider: AccessTokenProvider[F], tokenIntrospection: TokenIntrospection[F]): ClientCredentialsProvider[F] =
    new ClientCredentialsProvider[F] {
      override def requestToken(scope: Scope): F[ClientCredentialsToken.AccessTokenResponse] =
        accessTokenProvider.requestToken(scope)

      override def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse] =
        tokenIntrospection.introspect(token)

    }

}
