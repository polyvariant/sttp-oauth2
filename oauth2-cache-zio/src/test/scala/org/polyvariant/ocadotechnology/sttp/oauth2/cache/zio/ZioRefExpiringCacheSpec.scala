package org.polyvariant.sttp.oauth2.cache.zio

import zio.Clock
import zio.{Duration => ZDuration}
import zio.test._

import scala.concurrent.duration._

object ZioRefExpiringCacheSpec extends ZIOSpecDefault {

  private val someKey = "key"
  private val someValue = 1

  def spec = suite("Cache")(
    test("return nothing on empty cache") {
      for {
        cache <- ZioRefExpiringCache[String, Int]
        value <- cache.get(someKey)
      } yield assert(value)(Assertion.isNone)
    },
    test("store and retrieve value immediately") {
      for {
        cache <- ZioRefExpiringCache[String, Int]
        now   <- Clock.instant
        _     <- cache.put(someKey, someValue, now.plusSeconds(60))
        value <- cache.get(someKey)
      } yield assert(value)(Assertion.isSome(Assertion.equalTo(someValue)))
    },
    test("return value right before expiration boundary") {
      for {
        cache <- ZioRefExpiringCache[String, Int]
        now   <- Clock.instant
        _     <- cache.put(someKey, someValue, now.plusSeconds(60))
        _     <- TestClock.adjust(ZDuration.fromScala(60.seconds - 1.nano))
        value <- cache.get(someKey)
      } yield assert(value)(Assertion.isSome(Assertion.equalTo(someValue)))
    },
    test("not return value if expired") {
      for {
        cache <- ZioRefExpiringCache[String, Int]
        now   <- Clock.instant
        _     <- cache.put(someKey, someValue, now.plusSeconds(60))
        _     <- TestClock.adjust(ZDuration.fromScala(60.seconds))
        value <- cache.get(someKey)
      } yield assert(value)(Assertion.isNone)
    },
    test("remove value on expired get") {
      for {
        cache  <- ZioRefExpiringCache[String, Int]
        now    <- Clock.instant
        _      <- cache.put(someKey, someValue, now.plusSeconds(60))
        _      <- TestClock.adjust(ZDuration.fromScala(60.seconds))
        value1 <- cache.get(someKey) // this call should remove expired value from cache
        _      <- TestClock.adjust(ZDuration.fromScala(-10.seconds)) // travel back in time, so in fact token is not expired yet
        value2 <- cache.get(someKey)
      } yield assert(value1)(Assertion.isNone) && assert(value2)(Assertion.isNone)
    }
  )

}
