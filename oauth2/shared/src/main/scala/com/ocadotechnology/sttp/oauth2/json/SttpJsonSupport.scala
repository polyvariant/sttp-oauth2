package com.ocadotechnology.sttp.oauth2.json

import sttp.client3.ResponseAs
import sttp.client3.ResponseException
import sttp.client3.asString

object SttpJsonSupport {
  def asJson[B: JsonDecoder]: ResponseAs[Either[ResponseException[String, JsonDecoder.Error], B], Any] =
    asString.mapWithMetadata(ResponseAs.deserializeRightWithError(JsonDecoder[B].decodeString)).showAs("either(as string, as json)")
}
