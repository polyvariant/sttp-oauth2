package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder

import scala.concurrent.duration.FiniteDuration

case class Oauth2TokenResponse(
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

object Oauth2TokenResponse {
  import com.ocadotechnology.sttp.oauth2.circe._

  implicit val decoder: Decoder[Oauth2TokenResponse] =
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
    )(Oauth2TokenResponse.apply)

}
