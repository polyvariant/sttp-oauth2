package com.ocadotechnology.sttp.oauth2.json.jsoniter

import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.json.JsonSpec
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken
import com.ocadotechnology.sttp.oauth2.ExtendedOAuth2TokenResponse
import com.ocadotechnology.sttp.oauth2.Introspection
import com.ocadotechnology.sttp.oauth2.RefreshTokenResponse
import com.ocadotechnology.sttp.oauth2.UserInfo
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import com.ocadotechnology.sttp.oauth2.json.jsoniter.instances._

class JsoniterJsonSpec extends JsonSpec {
  override protected implicit def tokenIntrospectionResponseJsonDecoder: JsonDecoder[Introspection.TokenIntrospectionResponse] = jsonDecoder

  override protected implicit def oAuth2ErrorJsonDecoder: JsonDecoder[OAuth2Error] = jsonDecoder

  override protected implicit def extendedOAuth2TokenResponseJsonDecoder: JsonDecoder[ExtendedOAuth2TokenResponse] = jsonDecoder

  override protected implicit def refreshTokenResponseJsonDecoder: JsonDecoder[RefreshTokenResponse] = jsonDecoder

  override protected implicit def userInfoJsonDecoder: JsonDecoder[UserInfo] = jsonDecoder

  override protected implicit def accessTokenResponseJsonDecoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse] = jsonDecoder
}
