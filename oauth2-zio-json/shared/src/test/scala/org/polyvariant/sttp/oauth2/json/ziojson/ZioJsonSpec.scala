package org.polyvariant.sttp.oauth2.json.ziojson

import org.polyvariant.sttp.oauth2.json.JsonSpec
import org.polyvariant.sttp.oauth2.json.ziojson.instances._
import org.polyvariant.sttp.oauth2.json.JsonDecoder
import org.polyvariant.sttp.oauth2.Introspection.TokenIntrospectionResponse
import org.polyvariant.sttp.oauth2.common._
import org.polyvariant.sttp.oauth2.ClientCredentialsToken
import org.polyvariant.sttp.oauth2.ExtendedOAuth2TokenResponse
import org.polyvariant.sttp.oauth2.RefreshTokenResponse
import org.polyvariant.sttp.oauth2.UserInfo

class ZioJsonSpec extends JsonSpec {

  protected implicit def tokenIntrospectionResponseJsonDecoder: JsonDecoder[TokenIntrospectionResponse] = jsonDecoder

  protected implicit def oAuth2ErrorJsonDecoder: JsonDecoder[Error.OAuth2Error] = jsonDecoder

  protected implicit def extendedOAuth2TokenResponseJsonDecoder: JsonDecoder[ExtendedOAuth2TokenResponse] = jsonDecoder

  protected implicit def refreshTokenResponseJsonDecoder: JsonDecoder[RefreshTokenResponse] = jsonDecoder

  protected implicit def userInfoJsonDecoder: JsonDecoder[UserInfo] = jsonDecoder

  protected implicit def accessTokenResponseJsonDecoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse] = jsonDecoder

}
