package com.ocadotechnology.sttp.oauth2.cache.scalacache

import cats.effect.IO
import cats.effect.kernel.Outcome.Succeeded
import cats.effect.testkit.TestContext
import cats.effect.testkit.TestInstances
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scalacache.caffeine._

import scala.concurrent.duration._

class ScalacacheExpiringCacheSpec extends AnyWordSpec with Matchers with TestInstances {
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
        cacheBackend <- CaffeineCache[IO, String, Int]
        cache = ScalacacheExpiringCache[IO, String, Int](cacheBackend)
        value        <- cache.get(someKey)
      } yield value
    }.shouldBe(Succeeded(Some(None)))

    "store and retrieve value immediately" in unsafeRun {
      for {
        cacheBackend <- CaffeineCache[IO, String, Int]
        cache = ScalacacheExpiringCache[IO, String, Int](cacheBackend)
        now          <- IO.realTimeInstant
        _            <- cache.put(someKey, someValue, now.plusSeconds(60))
        value        <- cache.get(someKey)
      } yield value
    }.shouldBe(Succeeded(Some(Some(someValue))))

    "return value right before expiration boundary" in unsafeRun {
      for {
        cacheBackend <- CaffeineCache[IO, String, Int]
        cache = ScalacacheExpiringCache[IO, String, Int](cacheBackend)
        now          <- IO.realTimeInstant
        _            <- cache.put(someKey, someValue, now.plusSeconds(60))
        _            <- IO.sleep(60.seconds - 1.nano)
        value        <- cache.get(someKey)
      } yield value
    }.shouldBe(Succeeded(Some(Some(someValue))))

    "not return value if expired" in unsafeRun {
      for {
        cacheBackend <- CaffeineCache[IO, String, Int]
        cache = ScalacacheExpiringCache[IO, String, Int](cacheBackend)
        now          <- IO.realTimeInstant
        _            <- cache.put(someKey, someValue, now.plusSeconds(60))
        _            <- IO.sleep(60.seconds)
        value        <- cache.get(someKey)
      } yield value
    }.shouldBe(Succeeded(Some(None)))

    "remove value when expired" in unsafeRun {
      for {
        cacheBackend <- CaffeineCache[IO, String, Int]
        cache = ScalacacheExpiringCache[IO, String, Int](cacheBackend)
        now          <- IO.realTimeInstant
        _            <- cache.put(someKey, someValue, now.plusSeconds(1))
        _            <- IO.sleep(3.seconds)
        value        <- cache.get(someKey)
      } yield value
    }.shouldBe(Succeeded(Some(None)))

  }
}
