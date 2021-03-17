package com.ocadotechnology.sttp.oauth2.backend

import monix.execution.atomic.AtomicAny

import scala.concurrent.Future

final class MonixFutureCache[A] extends Cache[Future, A] {
  private val ref: AtomicAny[Option[A]] = AtomicAny(Option.empty[A])

  override def get: Future[Option[A]] = Future.successful(ref.get())

  override def set(a: A): Future[Unit] = Future.successful(ref.set(Some(a)))
}
