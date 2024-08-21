package org.polyvariant.sttp.oauth2

import cats.implicits._
import org.polyvariant.sttp.oauth2.common._
import org.polyvariant.sttp.oauth2.common.Error.OAuth2Error
import org.polyvariant.sttp.oauth2.json.JsonDecoder
import eu.timepit.refined.types.string.NonEmptyString
import sttp.client3.SttpBackend
import sttp.model.Uri
import sttp.monad.MonadError
import sttp.monad.syntax._

trait AccessTokenProvider[F[_]] {

  /** Request new token with given scope from OAuth2 provider.
    *
    * The scope is the scope of the application we want to communicate with.
    */
  def requestToken(scope: Option[Scope]): F[ClientCredentialsToken.AccessTokenResponse]
}

object AccessTokenProvider {

  /** Create instance of AccessTokenProvider with sttp backend.
    *
    * `clientId`, `clientSecret` are parameters of your application.
    */
  def apply[F[_]](
    tokenUrl: Uri,
    clientId: NonEmptyString,
    clientSecret: Secret[String]
  )(
    backend: SttpBackend[F, Any]
  )(
    implicit decoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): AccessTokenProvider[F] =
    new AccessTokenProvider[F] {
      implicit val F: MonadError[F] = backend.responseMonad

      override def requestToken(scope: Option[Scope]): F[ClientCredentialsToken.AccessTokenResponse] =
        ClientCredentials
          .requestToken(tokenUrl, clientId, clientSecret, scope)(backend)
          .map(_.leftMap(OAuth2Exception.apply))
          .flatMap(_.fold(F.error, F.unit))

    }

}
