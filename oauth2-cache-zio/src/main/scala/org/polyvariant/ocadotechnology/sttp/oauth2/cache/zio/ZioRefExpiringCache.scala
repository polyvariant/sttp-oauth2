package org.polyvariant.sttp.oauth2.cache.zio

import org.polyvariant.sttp.oauth2.cache.ExpiringCache
import org.polyvariant.sttp.oauth2.cache.zio.ZioRefExpiringCache.Entry
import zio.Clock
import zio.Ref
import zio.Task
import zio.ZIO

import java.time.Instant

final class ZioRefExpiringCache[K, V] private (ref: Ref[Map[K, Entry[V]]]) extends ExpiringCache[Task, K, V] {

  override def get(key: K): Task[Option[V]] =
    ref.get.map(_.get(key)).flatMap { entry =>
      Clock.instant.flatMap { now =>
        (entry, now) match {
          case (Some(Entry(value, expiryInstant)), now) =>
            if (now.isBefore(expiryInstant)) ZIO.succeed(Some(value)) else remove(key).as(None)
          case _                                        =>
            ZIO.none
        }
      }
    }

  override def put(key: K, value: V, expirationTime: Instant): Task[Unit] = ref.update(_ + (key -> Entry(value, expirationTime)))

  override def remove(key: K): Task[Unit] = ref.update(_ - key)
}

object ZioRefExpiringCache {
  private final case class Entry[V](value: V, expirationTime: Instant)

  def apply[K, V]: Task[ExpiringCache[Task, K, V]] = Ref.make(Map.empty[K, Entry[V]]).map(new ZioRefExpiringCache(_))
}
