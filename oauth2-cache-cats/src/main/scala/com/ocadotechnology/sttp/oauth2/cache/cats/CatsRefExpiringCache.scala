package com.ocadotechnology.sttp.oauth2.cache.cats

import cats.Monad
import cats.data.OptionT
import cats.effect.kernel.Clock
import cats.effect.kernel.Ref
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.cache.ExpiringCache
import com.ocadotechnology.sttp.oauth2.cache.cats.CatsRefExpiringCache.Entry

import java.time.Instant

final class CatsRefExpiringCache[F[_]: Monad: Clock, K, V] private[cats] (
  ref: Ref[F, Map[K, Entry[V]]]
) extends ExpiringCache[F, K, V] {

  override def get(
    key: K
  ): F[Option[V]] =
    OptionT(ref.get.map(_.get(key)))
      .product(OptionT.liftF(Clock[F].realTimeInstant))
      .flatMapF { case (Entry(value, expiryInstant), now) =>
        if (now.isBefore(expiryInstant))
          value.some.pure[F]
        else
          remove(key) *> none[V].pure[F] // cleaning up to save memory
      }
      .value

  override def put(
    key: K,
    value: V,
    expirationTime: Instant
  ): F[Unit] = ref.update(_ + (key -> Entry(value, expirationTime)))

  override def remove(
    key: K
  ): F[Unit] = ref.update(_ - key)

}

object CatsRefExpiringCache {

  private[cats] final case class Entry[V](
    value: V,
    expirationTime: Instant
  )

  def apply[F[_]: Ref.Make: Monad: Clock, K, V]: F[ExpiringCache[F, K, V]] =
    Ref[F].of(Map.empty[K, Entry[V]]).map(new CatsRefExpiringCache(_))
}
