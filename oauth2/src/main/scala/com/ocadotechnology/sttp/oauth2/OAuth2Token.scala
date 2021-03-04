package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common.Error
import io.circe.Decoder
import sttp.client3.ResponseAs
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error

object OAuth2Token {

  type Response = Either[Error, Oauth2TokenResponse]

  private implicit val bearerTokenResponseDecoder: Decoder[Either[OAuth2Error, Oauth2TokenResponse]] =
    circe.eitherOrFirstError[Oauth2TokenResponse, OAuth2Error](
      Decoder[Oauth2TokenResponse],
      Decoder[OAuth2Error]
    )

  val response: ResponseAs[Response, Any] =
    common.responseWithCommonError[Oauth2TokenResponse]

}
