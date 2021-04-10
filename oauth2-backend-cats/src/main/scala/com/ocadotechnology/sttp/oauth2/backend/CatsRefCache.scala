package com.ocadotechnology.sttp.oauth2.backend

import cats.Functor
import cats.effect.kernel.Ref
import cats.implicits._

final class CatsRefCache[F[_], A] private (ref: Ref[F, Option[A]]) extends Cache[F, A] {
  override def get: F[Option[A]] = ref.get
  override def set(a: A): F[Unit] = ref.set(Some(a))
}

object CatsRefCache {

  def apply[F[_]: Ref.Make: Functor, A]: F[Cache[F, A]] = Ref[F].of(Option.empty[A]).map(new CatsRefCache(_))
}
