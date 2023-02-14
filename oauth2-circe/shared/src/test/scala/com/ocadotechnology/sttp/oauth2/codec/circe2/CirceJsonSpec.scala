package com.ocadotechnology.sttp.oauth2.codec.circe2 // TODO fix the package

import com.ocadotechnology.sttp.oauth2.json.JsonSpec
import com.ocadotechnology.sttp.oauth2.codec.circe._
import com.ocadotechnology.sttp.oauth2.codec.EntityDecoder

import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.ExtendedOAuth2TokenResponse
import com.ocadotechnology.sttp.oauth2.RefreshTokenResponse
import com.ocadotechnology.sttp.oauth2.UserInfo
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken

class CirceJsonSpec extends JsonSpec {

  protected implicit def tokenIntrospectionResponseEntityDecoder: EntityDecoder[TokenIntrospectionResponse] = entityDecoder

  protected implicit def oAuth2ErrorEntityDecoder: EntityDecoder[Error.OAuth2Error] = entityDecoder

  protected implicit def extendedOAuth2TokenResponseEntityDecoder: EntityDecoder[ExtendedOAuth2TokenResponse] = entityDecoder

  protected implicit def refreshTokenResponseEntityDecoder: EntityDecoder[RefreshTokenResponse] = entityDecoder

  protected implicit def userInfoEntityDecoder: EntityDecoder[UserInfo] = entityDecoder

  protected implicit def accessTokenResponseEntityDecoder: EntityDecoder[ClientCredentialsToken.AccessTokenResponse] = entityDecoder

}
