package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.DurationLong

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

  def toOauth2Token(oldRefreshToken: String) =
    Oauth2TokenResponse(
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
  implicit val circeConf: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults
  implicit val decoder: Decoder[RefreshTokenResponse] = deriveConfiguredDecoder[RefreshTokenResponse]
  implicit val decoderFiniteDuration: Decoder[FiniteDuration] = Decoder.decodeLong.map(DurationLong(_).seconds)
}
