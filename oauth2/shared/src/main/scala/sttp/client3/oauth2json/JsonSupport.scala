package sttp.client3.oauth2json

import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import sttp.client3.IsOption
import sttp.client3.ResponseAs
import sttp.client3.ResponseException
import sttp.client3.asString
import sttp.client3.JsonInput.sanitize
import sttp.client3.json.RichResponseAs

object JsonSupport {
  def asJson[B: JsonDecoder: IsOption]: ResponseAs[Either[ResponseException[String, JsonDecoder.Error], B], Any] =
    asString.mapWithMetadata(ResponseAs.deserializeRightWithError(deserializeJson)).showAsJson

  private def deserializeJson[B: JsonDecoder: IsOption]: String => Either[JsonDecoder.Error, B] =
    sanitize[B].andThen(JsonDecoder[B].decodeString)
}
