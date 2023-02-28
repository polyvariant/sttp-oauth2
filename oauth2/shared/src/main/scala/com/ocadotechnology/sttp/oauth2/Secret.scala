package com.ocadotechnology.sttp.oauth2

final class Secret[A] protected (val value: A) {

  val valueHashModulo: Int =
    value.hashCode % 8191 // 2^13 -1

  override def toString: String =
    s"Secret($valueHashModulo)"

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
}
