package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

case class TokenUserDetails(
  username: String,
  name: String,
  forename: String,
  surname: String,
  mail: String,
  cn: String,
  sn: String,
  givenName: String
)

object TokenUserDetails {
  implicit val circeConf: Configuration = Configuration.default
  implicit val decoder: Decoder[TokenUserDetails] = deriveConfiguredDecoder[TokenUserDetails]
}
