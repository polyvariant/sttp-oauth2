package com.ocadotechnology.sttp.oauth2

import cats.MonadError
import com.ocadotechnology.sttp.oauth2.common._
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client.NothingT
import sttp.client.SttpBackend
import sttp.model.Uri
import cats.syntax.all._

/**
  * Tagless Final algebra fo ClientCredentials token requests and verification.
  */
trait ClientCredentialsProvider[F[_]] {

  /**
    * Request new token with given scope from OAuth2 provider.
    *
    * The scope is the scope of the application we want to communicate with.
    */
  def requestToken(scope: Scope): F[ClientCredentialsToken.AccessTokenResponse]

  /**
    * Introspects passed token in OAuth2 provider.
    *
    * Successful introspections returns `F[TokenIntrospectionResponse.IntrospectionResponse]`.
    */
  def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse]

}

object ClientCredentialsProvider {

  /**
    * Create instance of auth provider with sttp backend.
    *
    * `clientId`, `clientSecret`, `applicationScope` are parameters of your application.
    */
  def instance[F[_]: MonadError[*[_], Throwable]](
    tokenUrl: Uri,
    tokenIntrospectionUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    implicit sttpBackend: SttpBackend[F, Nothing, NothingT]
  ): ClientCredentialsProvider[F] =
    new ClientCredentialsProvider[F] {

      override def requestToken(scope: Scope): F[ClientCredentialsToken.AccessTokenResponse] =
        ClientCredentials.requestToken(tokenUrl, clientId, clientSecret, scope)(sttpBackend).map(_.leftMap(OAuth2Exception)).rethrow

      override def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse] =
        ClientCredentials
          .introspectToken(tokenIntrospectionUrl, clientId, clientSecret, token)(sttpBackend)
          .map(_.leftMap(OAuth2Exception))
          .rethrow

    }

}
