package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common.Error
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import sttp.client4.ResponseAs

object OAuth2Token {

  // TODO: should be changed to Response[A] and allow custom responses, like in AuthorizationCodeGrant
  type Response = Either[Error, ExtendedOAuth2TokenResponse]

  def response(
    implicit decoder: JsonDecoder[ExtendedOAuth2TokenResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): ResponseAs[Response] =
    common.responseWithCommonError[ExtendedOAuth2TokenResponse]

}
