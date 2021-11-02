package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common.Error
import sttp.client3.ResponseAs

object OAuth2Token {

  // TODO: should be changed to Response[A] and allow custom responses, like in AuthorizationCodeGrant
  type Response = Either[Error, ExtendedOAuth2TokenResponse]

  val response: ResponseAs[Response, Any] =
    common.responseWithCommonError[ExtendedOAuth2TokenResponse]

}
