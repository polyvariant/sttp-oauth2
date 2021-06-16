package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder

import scala.concurrent.duration.FiniteDuration

case class OAuth2TokenResponse(
  accessToken: Secret[String],
  scope: String,
  tokenType: String,
  expiresIn: Option[FiniteDuration],
  refreshToken: Option[String]
)
object OAuth2TokenResponse {
  import com.ocadotechnology.sttp.oauth2.circe._

  implicit val decoder: Decoder[OAuth2TokenResponse] =
    Decoder.forProduct5(
      "access_token",
      "scope",
      "token_type",
      "expires_in",
      "refresh_token"
    )(OAuth2TokenResponse.apply)

}

// @deprecated("This model will be removed in next release", "0.10.0")
case class ExtendedOAuth2TokenResponse(
  accessToken: Secret[String],
  refreshToken: String,
  expiresIn: FiniteDuration,
  userName: String,
  domain: String,
  userDetails: TokenUserDetails,
  roles: Set[String],
  scope: String,
  securityLevel: Long,
  userId: String,
  tokenType: String
)

object ExtendedOAuth2TokenResponse {
  import com.ocadotechnology.sttp.oauth2.circe._

  implicit val decoder: Decoder[ExtendedOAuth2TokenResponse] =
    Decoder.forProduct11(
      "access_token",
      "refresh_token",
      "expires_in",
      "user_name",
      "domain",
      "user_details",
      "roles",
      "scope",
      "security_level",
      "user_id",
      "token_type"
    )(ExtendedOAuth2TokenResponse.apply)

}
