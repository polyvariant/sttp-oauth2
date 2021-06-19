package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common.Error
import io.circe.Decoder
import sttp.client3.ResponseAs
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error

object OAuth2Token {

  // TODO: should be changed to Response[A] and allow custom responses, like in AuthorizationCodeGrant
  type Response = Either[Error, ExtendedOAuth2TokenResponse]

  private implicit val bearerTokenResponseDecoder: Decoder[Either[OAuth2Error, ExtendedOAuth2TokenResponse]] =
    circe.eitherOrFirstError[ExtendedOAuth2TokenResponse, OAuth2Error](
      Decoder[ExtendedOAuth2TokenResponse],
      Decoder[OAuth2Error]
    )

  val response: ResponseAs[Response, Any] =
    common.responseWithCommonError[ExtendedOAuth2TokenResponse]

}
