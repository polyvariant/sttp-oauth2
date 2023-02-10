package com.ocadotechnology.sttp.oauth2

case class TokenUserDetails(
  username: String,
  name: String,
  forename: String,
  surname: String,
  mail: String,
  cn: String,
  sn: String
)
