package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.DurationLong

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
  implicit val circeConf: Configuration = Configuration.default.withSnakeCaseMemberNames.withDefaults
  implicit val decoder: Decoder[Oauth2TokenResponse] = deriveConfiguredDecoder[Oauth2TokenResponse]
  implicit val decoderFiniteDuration: Decoder[FiniteDuration] = Decoder.decodeLong.map(DurationLong(_).seconds)
}
