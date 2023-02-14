package com.ocadotechnology.sttp.oauth2.json

import com.ocadotechnology.sttp.oauth2.codec.EntityDecoder
import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.ExtendedOAuth2TokenResponse
import com.ocadotechnology.sttp.oauth2.RefreshTokenResponse
import com.ocadotechnology.sttp.oauth2.UserInfo
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken

trait JsonDecoders {
  protected implicit def tokenIntrospectionResponseEntityDecoder: EntityDecoder[TokenIntrospectionResponse]
  protected implicit def oAuth2ErrorEntityDecoder: EntityDecoder[Error.OAuth2Error]
  protected implicit def extendedOAuth2TokenResponseEntityDecoder: EntityDecoder[ExtendedOAuth2TokenResponse]
  protected implicit def refreshTokenResponseEntityDecoder: EntityDecoder[RefreshTokenResponse]
  protected implicit def userInfoEntityDecoder: EntityDecoder[UserInfo]
  protected implicit def accessTokenResponseEntityDecoder: EntityDecoder[ClientCredentialsToken.AccessTokenResponse]
}
