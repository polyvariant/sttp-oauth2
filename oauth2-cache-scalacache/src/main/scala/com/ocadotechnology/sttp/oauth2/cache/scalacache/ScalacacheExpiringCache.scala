package com.ocadotechnology.sttp.oauth2.cache.scalacache

import cats.effect.Async
import cats.effect.kernel.Clock
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.cache.ExpiringCache
import scalacache._

import java.time.Instant
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

final class ScalacacheExpiringCache[F[_]: Async, K, V](cache: Cache[F, K, V]) extends ExpiringCache[F, K, V] {

  override def get(key: K): F[Option[V]] =
    cache.get(key)

  override def put(key: K, value: V, expirationTime: Instant): F[Unit] =
    for {
      now <- Clock[F].realTimeInstant
      ttl = calculateTTL(expirationTime, now)
      _   <- cache.put(key)(value, Some(ttl)).void
    } yield ()

  override def remove(key: K): F[Unit] =
    cache.remove(key).void

  private def calculateTTL(expirationTime: Instant, now: Instant): FiniteDuration =
    if (expirationTime isAfter now)
      FiniteDuration(expirationTime.toEpochMilli() - now.toEpochMilli(), TimeUnit.MILLISECONDS)
    else FiniteDuration(0, TimeUnit.MILLISECONDS)

}

object ScalacacheExpiringCache {

  def apply[F[_]: Async, K, V](cache: Cache[F, K, V]): ExpiringCache[F, K, V] =
    new ScalacacheExpiringCache[F, K, V](cache)

}
