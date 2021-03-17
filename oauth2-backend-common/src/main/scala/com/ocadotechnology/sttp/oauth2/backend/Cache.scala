package com.ocadotechnology.sttp.oauth2.backend

trait Cache[F[_], A] {
  def get: F[Option[A]]
  def set(a: A): F[Unit]
}
