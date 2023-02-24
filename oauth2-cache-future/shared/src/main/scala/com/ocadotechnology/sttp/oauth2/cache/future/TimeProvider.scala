package com.ocadotechnology.sttp.oauth2.cache.future

import java.time.Instant

trait TimeProvider {
  def currentInstant(
  ): Instant
}

object TimeProvider {

  val default: TimeProvider = new TimeProvider {
    override def currentInstant(
    ): Instant = Instant.now()
  }

}
