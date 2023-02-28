package com.ocadotechnology.sttp.oauth2.cache.cats

import cats.effect.IO
import cats.effect.kernel.Outcome.Succeeded
import cats.effect.testkit.TestContext
import cats.effect.testkit.TestInstances
import com.ocadotechnology.sttp.oauth2.cache.cats.CatsRefExpiringCache.Entry
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

class CatsRefExpiringCacheSpec extends AnyWordSpec with Matchers with TestInstances {
  private implicit val ticker: Ticker = Ticker(TestContext())

  private val someKey = "key"
  private val someValue = 1

  def runTest(test: IO[Assertion]): Assertion =
    unsafeRun(test) match {
      case Succeeded(Some(assertion)) => assertion
      case wrongResult                => fail(s"Test should finish successfully. Instead ended with $wrongResult")
    }

  "Cache" should {
    "return nothing on empty cache" in unsafeRun {
      for {
        cache <- CatsRefExpiringCache[IO, String, Int]
        value <- cache.get(someKey)
      } yield value
    }.shouldBe(Succeeded(Some(None)))

    "store and retrieve value immediately" in unsafeRun {
      for {
        cache <- CatsRefExpiringCache[IO, String, Int]
        now   <- IO.realTimeInstant
        _     <- cache.put(someKey, someValue, now.plusSeconds(60))
        value <- cache.get(someKey)
      } yield value
    }.shouldBe(Succeeded(Some(Some(someValue))))

    "return value right before expiration boundary" in unsafeRun {
      for {
        cache <- CatsRefExpiringCache[IO, String, Int]
        now   <- IO.realTimeInstant
        _     <- cache.put(someKey, someValue, now.plusSeconds(60))
        _     <- IO.sleep(60.seconds - 1.nano)
        value <- cache.get(someKey)
      } yield value
    }.shouldBe(Succeeded(Some(Some(someValue))))

    "not return value if expired" in unsafeRun {
      for {
        cache <- CatsRefExpiringCache[IO, String, Int]
        now   <- IO.realTimeInstant
        _     <- cache.put(someKey, someValue, now.plusSeconds(60))
        _     <- IO.sleep(60.seconds)
        value <- cache.get(someKey)
      } yield value
    }.shouldBe(Succeeded(Some(None)))

    "remove value on expired get" in unsafeRun {
      for {
        innerStateRef      <- IO.ref(Map.empty[String, Entry[Int]])
        cache = new CatsRefExpiringCache(innerStateRef)
        now                <- IO.realTimeInstant
        _                  <- cache.put(someKey, someValue, now.plusSeconds(60))
        _                  <- IO.sleep(60.seconds)
        value              <- cache.get(someKey) // this call should remove expired value from cache
        innerStateAfterGet <- innerStateRef.get
      } yield (value, innerStateAfterGet)
    }.shouldBe(Succeeded(Some((None, Map()))))

  }
}
