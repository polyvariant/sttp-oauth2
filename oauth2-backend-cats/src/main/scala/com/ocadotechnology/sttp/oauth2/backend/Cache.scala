package com.ocadotechnology.sttp.oauth2.backend

import cats.effect.Sync
import cats.implicits._
import cats.effect.concurrent.Ref

trait Cache[F[_], A] {
  def get: F[Option[A]]
  def set(a: A): F[Unit]
}

object Cache {

  def refCache[F[_]: Sync, A]: F[Cache[F, A]] = Ref[F].of(Option.empty[A]).map { ref =>
    new Cache[F, A] {
      override def get: F[Option[A]] = ref.get
      override def set(a: A): F[Unit] = ref.set(Some(a))
    }
  }

}
