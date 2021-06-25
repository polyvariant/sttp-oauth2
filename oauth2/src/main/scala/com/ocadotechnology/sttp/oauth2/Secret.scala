package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder

final class Secret[A] protected (val value: A) {

  val valueHashModuloRenamed: Int =
    value.hashCode % 8191 // 2^13 -1

  override def toString: String =
    s"Secret($valueHashModuloRenamed)"

  override def hashCode: Int =
    value.hashCode

  override def equals(that: Any): Boolean =
    that match {
      case Secret(thatValue) => value == thatValue
      case _                 => false
    }

}

object Secret {
  def apply[A](value: A) = new Secret(value)

  def unapply[A](secret: Secret[A]): Option[A] = Some(secret.value)

  implicit def secretDecoder[A: Decoder]: Decoder[Secret[A]] = Decoder[A].map(Secret(_))
}
