package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client3.SttpBackend
import sttp.model.Uri
import sttp.monad.MonadError
import sttp.monad.syntax._

trait TokenIntrospection[F[_]] {

  /** Introspects passed token in OAuth2 provider.
    *
    * Successful introspections returns `F[TokenIntrospectionResponse.IntrospectionResponse]`.
    */
  def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse]

}

object TokenIntrospection {

  /** Create instance of TokenInstrospection with sttp backend.
    *
    * `clientId`, `clientSecret` are parameters of your application.
    */
  def apply[F[_]](
    tokenIntrospectionUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    backend: SttpBackend[F, Any]
  )(
    implicit decoder: JsonDecoder[TokenIntrospectionResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): TokenIntrospection[F] =
    new TokenIntrospection[F] {
      implicit val F: MonadError[F] = backend.responseMonad

      override def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse] =
        ClientCredentials
          .introspectToken(tokenIntrospectionUrl, clientId, clientSecret, token)(backend)
          .map(_.leftMap(OAuth2Exception.apply))
          .flatMap(_.fold(F.error, F.unit))

    }

}
