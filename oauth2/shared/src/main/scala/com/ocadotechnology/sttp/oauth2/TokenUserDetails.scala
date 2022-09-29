package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder

case class TokenUserDetails(
  username: String,
  name: String,
  forename: String,
  surname: String,
  mail: String,
  cn: String,
  sn: String
)

object TokenUserDetails {

  implicit val decoder: Decoder[TokenUserDetails] =
    Decoder.forProduct7(
      "username",
      "name",
      "forename",
      "surname",
      "mail",
      "cn",
      "sn"
    )(TokenUserDetails.apply)

}
