package sttp.client3.oauth2json

import com.ocadotechnology.sttp.oauth2.codec.EntityDecoder
import sttp.client3.IsOption
import sttp.client3.ResponseAs
import sttp.client3.ResponseException
import sttp.client3.asString
import sttp.client3.JsonInput.sanitize
import sttp.client3.json.RichResponseAs

object EntityDecoderSupport {
  def asJson[B: EntityDecoder: IsOption]: ResponseAs[Either[ResponseException[String, EntityDecoder.Error], B], Any] =
    asString.mapWithMetadata(ResponseAs.deserializeRightWithError(deserializeJson)).showAsJson

  private def deserializeJson[B: EntityDecoder: IsOption]: String => Either[EntityDecoder.Error, B] =
    sanitize[B].andThen(EntityDecoder[B].decodeString)
}
