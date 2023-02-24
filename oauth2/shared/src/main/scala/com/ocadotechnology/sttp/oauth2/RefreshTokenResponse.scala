package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder

import scala.concurrent.duration.FiniteDuration

private[oauth2] final case class RefreshTokenResponse(
  accessToken: Secret[String],
  refreshToken: Option[String],
  expiresIn: FiniteDuration,
  userName: String,
  domain: String,
  userDetails: TokenUserDetails,
  roles: Set[String],
  scope: String,
  securityLevel: Long,
  userId: String,
  tokenType: String
) {

  def toOauth2Token(
    oldRefreshToken: String
  ): ExtendedOAuth2TokenResponse =
    ExtendedOAuth2TokenResponse(
      accessToken,
      refreshToken.getOrElse(oldRefreshToken),
      expiresIn,
      userName,
      domain,
      userDetails,
      roles,
      scope,
      securityLevel,
      userId,
      tokenType
    )

}

private[oauth2] object RefreshTokenResponse {

  import com.ocadotechnology.sttp.oauth2.circe._

  implicit val decoder: Decoder[RefreshTokenResponse] =
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
    )(RefreshTokenResponse.apply)

}
