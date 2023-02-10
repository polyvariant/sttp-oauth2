package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.codec.EntityDecoder
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.common.Error
import com.ocadotechnology.sttp.oauth2.common.Scope
import sttp.client3.ResponseAs

import scala.concurrent.duration.FiniteDuration

object ClientCredentialsToken {

  type Response = Either[Error, ClientCredentialsToken.AccessTokenResponse]

  def response(
    implicit decoder: EntityDecoder[ClientCredentialsToken.AccessTokenResponse],
    oAuth2ErrorDecoder: EntityDecoder[OAuth2Error]
  ): ResponseAs[Response, Any] =
    common.responseWithCommonError[ClientCredentialsToken.AccessTokenResponse]

  final case class AccessTokenResponse(
    accessToken: Secret[String],
    domain: Option[String],
    expiresIn: FiniteDuration,
    scope: Option[Scope]
  )

}
