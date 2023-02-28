package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder

object JsonDecoderMock {

  def partialFunction[A](f: PartialFunction[String, A]): JsonDecoder[A] = new JsonDecoder[A] {
    def decodeString(data: String): Either[JsonDecoder.Error, A] =
      f.lift(data).fold(new JsonDecoder.Error("Data does not match").asLeft[A])(_.asRight)
  }

  def failing[A] = new JsonDecoder[A] {

    def decodeString(data: String): Either[JsonDecoder.Error, A] = Left(
      new JsonDecoder.Error(s"This decoder fails deliberately, even on [$data]")
    )

  }

}
