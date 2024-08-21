package org.polyvariant.sttp.oauth2

import org.polyvariant.sttp.oauth2.common.Error
import org.polyvariant.sttp.oauth2.common.Error.OAuth2Error
import org.polyvariant.sttp.oauth2.json.JsonDecoder
import sttp.client3.ResponseAs

object OAuth2Token {

  // TODO: should be changed to Response[A] and allow custom responses, like in AuthorizationCodeGrant
  type Response = Either[Error, ExtendedOAuth2TokenResponse]

  def response(
    implicit decoder: JsonDecoder[ExtendedOAuth2TokenResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): ResponseAs[Response, Any] =
    common.responseWithCommonError[ExtendedOAuth2TokenResponse]

}
