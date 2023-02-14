package com.ocadotechnology.sttp.oauth2.json.circe

import com.ocadotechnology.sttp.oauth2.json.JsonSpec
import com.ocadotechnology.sttp.oauth2.json.circe.instances._
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder

import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.ExtendedOAuth2TokenResponse
import com.ocadotechnology.sttp.oauth2.RefreshTokenResponse
import com.ocadotechnology.sttp.oauth2.UserInfo
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken

class CirceJsonSpec extends JsonSpec {

  protected implicit def tokenIntrospectionResponseJsonDecoder: JsonDecoder[TokenIntrospectionResponse] = jsonDecoder

  protected implicit def oAuth2ErrorJsonDecoder: JsonDecoder[Error.OAuth2Error] = jsonDecoder

  protected implicit def extendedOAuth2TokenResponseJsonDecoder: JsonDecoder[ExtendedOAuth2TokenResponse] = jsonDecoder

  protected implicit def refreshTokenResponseJsonDecoder: JsonDecoder[RefreshTokenResponse] = jsonDecoder

  protected implicit def userInfoJsonDecoder: JsonDecoder[UserInfo] = jsonDecoder

  protected implicit def accessTokenResponseJsonDecoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse] = jsonDecoder

}
