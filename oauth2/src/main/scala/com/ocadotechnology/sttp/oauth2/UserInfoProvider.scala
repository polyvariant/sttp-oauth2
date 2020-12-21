package com.ocadotechnology.sttp.oauth2

import cats.MonadError
import cats.syntax.all._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import sttp.client.NothingT
import io.circe.parser.decode
import sttp.client._
import sttp.model.Uri

trait UserInfoProvider[F[_]] {
  def userInfo(accessToken: String): F[UserInfo]
}

object UserInfoProvider {
  def apply[F[_]](implicit ev: UserInfoProvider[F]): UserInfoProvider[F] = ev

  private def requestUserInfo[F[_]: MonadError[*[_], Throwable]](
    baseUrl: Uri,
    accessToken: String
  )(
    implicit backend: SttpBackend[F, Nothing, NothingT]
  ) =
    basicRequest
      .post(baseUrl.path(List("openid", "userinfo")))
      .header("Authorization", s"Bearer $accessToken")
      .response(asString)
      .send()
      .map(_.body.leftMap(new RuntimeException(_)).flatMap(decode[UserInfo]))
      .rethrow

  // TODO - add some description on what is expected of baseUrl
  def instance[F[_]: MonadError[*[_], Throwable]](
    baseUrl: Uri
  )(
    implicit backend: SttpBackend[F, Nothing, NothingT]
  ): UserInfoProvider[F] =
    (accessToken: String) => requestUserInfo(baseUrl, accessToken)

  // TODO - add some description on what is expected of baseUrl
  def instance[F[_]: MonadError[*[_], Throwable]](
    baseUrl: String Refined Url
  )(
    implicit backend: SttpBackend[F, Nothing, NothingT]
  ): UserInfoProvider[F] =
    (accessToken: String) => requestUserInfo(common.refinedUrlToUri(baseUrl), accessToken)
}
