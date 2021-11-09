package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.common._
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
  ): TokenIntrospection[F] =
    new TokenIntrospection[F] {
      implicit val F: MonadError[F] = backend.responseMonad

      override def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse] =
        ClientCredentials
          .introspectToken(tokenIntrospectionUrl, clientId, clientSecret, token)(backend)
          .map(_.leftMap(_.toException).toTry)
          .flatMap(backend.responseMonad.fromTry)

    }

}
