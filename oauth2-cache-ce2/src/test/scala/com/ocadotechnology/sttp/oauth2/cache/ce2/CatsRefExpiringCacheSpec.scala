package com.ocadotechnology.sttp.oauth2.cache.ce2

import cats.effect.Clock
import cats.effect.IO
import cats.effect.laws.util.TestContext
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.concurrent.duration._
import cats.effect.Temporal

class CatsRefExpiringCacheSpec extends AnyWordSpec with Matchers {
  implicit lazy val testContext: TestContext = TestContext.apply()
  implicit lazy val cs: ContextShift[IO] = IO.contextShift(testContext)
  implicit lazy val ioTimer: Temporal[IO] = testContext.timer[IO]

  private val someKey = "key"
  private val someValue = 1

  "Cache" should {
    "return nothing on empty cache" in {
      for {
        cache <- CatsRefExpiringCache[IO, String, Int]
        value <- cache.get(someKey)
      } yield value shouldBe None
    }.unsafeRunSync()

    "store and retrieve value immediately" in {
      for {
        cache <- CatsRefExpiringCache[IO, String, Int]
        now   <- Clock[IO].instantNow
        _     <- cache.put(someKey, someValue, now.plusSeconds(60))
        value <- cache.get(someKey)
      } yield value shouldBe Some(someValue)
    }.unsafeRunSync()

    "return value right before expiration boundary" in {
      for {
        cache <- CatsRefExpiringCache[IO, String, Int]
        now   <- Clock[IO].instantNow
        _     <- cache.put(someKey, someValue, now.plusSeconds(60))
        _ = testContext.tick(60.seconds - 1.nano)
        value <- cache.get(someKey)
      } yield value shouldBe Some(someValue)
    }.unsafeRunSync()

    "not return value if expired" in {
      for {
        cache <- CatsRefExpiringCache[IO, String, Int]
        now   <- Clock[IO].instantNow
        _     <- cache.put(someKey, someValue, now.plusSeconds(60))
        _ = testContext.tick(60.seconds)
        value <- cache.get(someKey)
      } yield value shouldBe None
    }.unsafeRunSync()

    "remove value on expired get" in {
      for {
        cache  <- CatsRefExpiringCache[IO, String, Int]
        now    <- Clock[IO].instantNow
        _      <- cache.put(someKey, someValue, now.plusSeconds(60))
        _ = testContext.tick(60.seconds)
        value1 <- cache.get(someKey) // this call should remove expired value from cache
        _ = testContext.tick(-10.seconds) // travel back in time, so in fact token is not expired yet
        value2 <- cache.get(someKey)
      } yield {
        value1 shouldBe None
        value2 shouldBe None
      }
    }.unsafeRunSync()
  }
}
