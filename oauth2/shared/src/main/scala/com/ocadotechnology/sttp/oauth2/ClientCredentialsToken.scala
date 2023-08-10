package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common.Error
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.common.Scope
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import sttp.client4.ResponseAs

import scala.concurrent.duration.FiniteDuration

object ClientCredentialsToken {

  type Response = Either[Error, ClientCredentialsToken.AccessTokenResponse]

  def response(
    implicit decoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): ResponseAs[Response] =
    common.responseWithCommonError[ClientCredentialsToken.AccessTokenResponse]

  final case class AccessTokenResponse(
    accessToken: Secret[String],
    domain: Option[String],
    expiresIn: FiniteDuration,
    scope: Option[Scope]
  )

}
