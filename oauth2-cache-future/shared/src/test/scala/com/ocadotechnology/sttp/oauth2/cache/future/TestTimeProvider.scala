package org.polyvariant.sttp.oauth2.cache.future

import java.time.Instant
import monix.execution.atomic.AtomicAny

class TestTimeProvider private (initial: Instant) extends TimeProvider {

  private val ref = AtomicAny(initial)

  def currentInstant(): Instant = ref.get()

  def updateInstant(newInstant: Instant): Unit = ref.set(newInstant)
}

object TestTimeProvider {
  def instance(initialInstant: Instant): TestTimeProvider = new TestTimeProvider(initialInstant)
}
