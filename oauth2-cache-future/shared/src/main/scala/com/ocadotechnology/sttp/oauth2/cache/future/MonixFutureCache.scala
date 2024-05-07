package org.polyvariant.sttp.oauth2.cache.future

import monix.execution.atomic.AtomicAny

import scala.concurrent.Future
import org.polyvariant.sttp.oauth2.cache.ExpiringCache
import org.polyvariant.sttp.oauth2.cache.future.MonixFutureCache.Entry
import java.time.Instant
import cats.implicits._
import scala.concurrent.ExecutionContext
import cats.data.OptionT

final class MonixFutureCache[K, V](timeProvider: TimeProvider)(implicit ec: ExecutionContext) extends ExpiringCache[Future, K, V] {
  private val ref: AtomicAny[Map[K, Entry[V]]] = AtomicAny(Map.empty[K, Entry[V]])

  override def get(key: K): Future[Option[V]] =
    getValue(key)
      .semiflatMap { case Entry(value, expirationTime) =>
        val now = timeProvider.currentInstant()

        if (now.isBefore(expirationTime))
          Future.successful(value.some)
        else {
          remove(key).map(_ => none)
        }
      }
      .getOrElse(none)

  private def getValue(key: K) = OptionT(Future(ref.get().get(key)))

  override def put(key: K, value: V, expirationTime: Instant): Future[Unit] = Future {
    ref.transform(_ + (key -> Entry(value, expirationTime)))
  }

  override def remove(key: K): Future[Unit] = Future {
    ref.transform(_ - key)
  }

}

object MonixFutureCache {
  final case class Entry[V](value: V, expirationTime: Instant)

  def apply[K, V](timeProvider: TimeProvider = TimeProvider.default)(implicit ec: ExecutionContext): MonixFutureCache[K, V] =
    new MonixFutureCache[K, V](timeProvider)
}
