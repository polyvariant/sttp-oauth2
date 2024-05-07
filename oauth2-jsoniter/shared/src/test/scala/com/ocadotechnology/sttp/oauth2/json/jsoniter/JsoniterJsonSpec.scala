package org.polyvariant.sttp.oauth2.json.jsoniter

import org.polyvariant.sttp.oauth2.common.Error.OAuth2Error
import org.polyvariant.sttp.oauth2.json.JsonSpec
import org.polyvariant.sttp.oauth2.ClientCredentialsToken
import org.polyvariant.sttp.oauth2.ExtendedOAuth2TokenResponse
import org.polyvariant.sttp.oauth2.Introspection
import org.polyvariant.sttp.oauth2.RefreshTokenResponse
import org.polyvariant.sttp.oauth2.UserInfo
import org.polyvariant.sttp.oauth2.json.JsonDecoder
import org.polyvariant.sttp.oauth2.json.jsoniter.instances._

class JsoniterJsonSpec extends JsonSpec {
  override protected implicit def tokenIntrospectionResponseJsonDecoder: JsonDecoder[Introspection.TokenIntrospectionResponse] = jsonDecoder

  override protected implicit def oAuth2ErrorJsonDecoder: JsonDecoder[OAuth2Error] = jsonDecoder

  override protected implicit def extendedOAuth2TokenResponseJsonDecoder: JsonDecoder[ExtendedOAuth2TokenResponse] = jsonDecoder

  override protected implicit def refreshTokenResponseJsonDecoder: JsonDecoder[RefreshTokenResponse] = jsonDecoder

  override protected implicit def userInfoJsonDecoder: JsonDecoder[UserInfo] = jsonDecoder

  override protected implicit def accessTokenResponseJsonDecoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse] = jsonDecoder
}
