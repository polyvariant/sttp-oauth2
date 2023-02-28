package com.ocadotechnology.sttp.oauth2.json

trait JsonDecoder[A] {

  def decode(
    data: EncodedJson
  ): Either[JsonDecoder.Error, A] = decodeString(data.value)

  def decodeString(
    data: String
  ): Either[JsonDecoder.Error, A]

}

object JsonDecoder {

  def apply[A](
    implicit ev: JsonDecoder[A]
  ): JsonDecoder[A] = ev

  final case class Error(
    message: String,
    cause: Option[Throwable] = None
  ) extends Exception(message, cause.orNull)

  object Error {

    def fromThrowable(
      throwable: Throwable
    ): Error = JsonDecoder.Error(throwable.getMessage, Some(throwable))

  }

}
