package com.ocadotechnology.sttp.oauth2.codec

trait EntityDecoder[A] {
  def decode(data: EncodedData): Either[EntityDecoder.Error, A] = decodeString(data.value)

  def decodeString(data: String): Either[EntityDecoder.Error, A]
}

object EntityDecoder {
  def apply[A](implicit ev: EntityDecoder[A]): EntityDecoder[A] = ev

  case class Error(message: String, cause: Option[Throwable] = None) extends Exception(message, cause.orNull)
}
