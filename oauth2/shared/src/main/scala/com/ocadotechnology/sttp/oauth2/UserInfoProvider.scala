package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import io.circe.parser.decode
import sttp.client3._
import sttp.model.Uri
import sttp.monad.MonadError
import sttp.monad.syntax._

trait UserInfoProvider[F[_]] {

  def userInfo(
    accessToken: String
  ): F[UserInfo]

}

object UserInfoProvider {

  def apply[F[_]](
    implicit ev: UserInfoProvider[F]
  ): UserInfoProvider[F] = ev

  private def requestUserInfo[F[_]](
    baseUrl: Uri,
    accessToken: String
  )(
    backend: SttpBackend[F, Any]
  ): F[UserInfo] = {
    implicit val F: MonadError[F] = backend.responseMonad
    backend
      .send {
        basicRequest
          .post(baseUrl.withPath("openid", "userinfo"))
          .header("Authorization", s"Bearer $accessToken")
          .response(asString)
      }
      .map(_.body.leftMap(new RuntimeException(_)).flatMap(decode[UserInfo]))
      .flatMap(_.fold(F.error, F.unit))
  }

  // TODO - add some description on what is expected of baseUrl
  def apply[F[_]](
    baseUrl: Uri
  )(
    backend: SttpBackend[F, Any]
  ): UserInfoProvider[F] =
    (accessToken: String) => requestUserInfo(baseUrl, accessToken)(backend)

  // TODO - add some description on what is expected of baseUrl
  def apply[F[_]](
    baseUrl: String Refined Url
  )(
    backend: SttpBackend[F, Any]
  ): UserInfoProvider[F] = UserInfoProvider[F](common.refinedUrlToUri(baseUrl))(backend)

}
