package org.polyvariant.sttp.oauth2.json

import org.polyvariant.sttp.oauth2.Introspection.TokenIntrospectionResponse
import org.polyvariant.sttp.oauth2.common._
import org.polyvariant.sttp.oauth2.ClientCredentialsToken
import org.polyvariant.sttp.oauth2.ExtendedOAuth2TokenResponse
import org.polyvariant.sttp.oauth2.RefreshTokenResponse
import org.polyvariant.sttp.oauth2.UserInfo

trait JsonDecoders {
  protected implicit def tokenIntrospectionResponseJsonDecoder: JsonDecoder[TokenIntrospectionResponse]
  protected implicit def oAuth2ErrorJsonDecoder: JsonDecoder[Error.OAuth2Error]
  protected implicit def extendedOAuth2TokenResponseJsonDecoder: JsonDecoder[ExtendedOAuth2TokenResponse]
  protected implicit def refreshTokenResponseJsonDecoder: JsonDecoder[RefreshTokenResponse]
  protected implicit def userInfoJsonDecoder: JsonDecoder[UserInfo]
  protected implicit def accessTokenResponseJsonDecoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse]
}
