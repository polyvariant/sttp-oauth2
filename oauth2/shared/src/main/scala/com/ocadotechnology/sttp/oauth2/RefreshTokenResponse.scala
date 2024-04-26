package org.polyvariant.sttp.oauth2

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

  def toOauth2Token(oldRefreshToken: String): ExtendedOAuth2TokenResponse =
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
