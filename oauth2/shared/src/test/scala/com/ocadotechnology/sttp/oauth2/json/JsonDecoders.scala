package com.ocadotechnology.sttp.oauth2.json

import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.ExtendedOAuth2TokenResponse
import com.ocadotechnology.sttp.oauth2.RefreshTokenResponse
import com.ocadotechnology.sttp.oauth2.UserInfo
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken

trait JsonDecoders {
  protected implicit def tokenIntrospectionResponseJsonDecoder: JsonDecoder[TokenIntrospectionResponse]
  protected implicit def oAuth2ErrorJsonDecoder: JsonDecoder[Error.OAuth2Error]
  protected implicit def extendedOAuth2TokenResponseJsonDecoder: JsonDecoder[ExtendedOAuth2TokenResponse]
  protected implicit def refreshTokenResponseJsonDecoder: JsonDecoder[RefreshTokenResponse]
  protected implicit def userInfoJsonDecoder: JsonDecoder[UserInfo]
  protected implicit def accessTokenResponseJsonDecoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse]
}
