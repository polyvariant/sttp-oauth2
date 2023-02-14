package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.codec.EntityDecoder
import cats.syntax.all._

object EntityDecoderMock {

  def partialFunction[A](f: PartialFunction[String, A]): EntityDecoder[A] = new EntityDecoder[A] {
    def decodeString(data: String): Either[EntityDecoder.Error, A] =
      f.unapply(data).fold(new EntityDecoder.Error("Data does not match").asLeft[A])(_.asRight)
  }

  def failing[A] = new EntityDecoder[A] {

    def decodeString(data: String): Either[EntityDecoder.Error, A] = Left(
      new EntityDecoder.Error(s"This decoder fails deliberately, even on [$data]")
    )

  }

}
