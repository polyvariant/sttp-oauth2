package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.codec.EntityDecoder
import com.ocadotechnology.sttp.oauth2.common.Error
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import sttp.client3.ResponseAs

object OAuth2Token {

  // TODO: should be changed to Response[A] and allow custom responses, like in AuthorizationCodeGrant
  type Response = Either[Error, ExtendedOAuth2TokenResponse]

  def response(implicit decoder: EntityDecoder[ExtendedOAuth2TokenResponse], oAuth2ErrorDecoder: EntityDecoder[OAuth2Error]): ResponseAs[Response, Any] =
    common.responseWithCommonError[ExtendedOAuth2TokenResponse]

}
