package org.polyvariant.sttp.oauth2.cache

import java.time.Instant

trait ExpiringCache[F[_], K, V] {
  def get(key: K): F[Option[V]]

  def put(key: K, value: V, expirationTime: Instant): F[Unit]

  def remove(key: K): F[Unit]
}
