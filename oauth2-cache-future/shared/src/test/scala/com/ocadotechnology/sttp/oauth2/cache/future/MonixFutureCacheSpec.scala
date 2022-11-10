package com.ocadotechnology.sttp.oauth2.cache.future

import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant

class MonixFutureCacheSpec extends AsyncWordSpec with Matchers {

  private val someKey = "key"
  private val someValue = 1
  private val someTime = Instant.parse("2021-10-03T10:15:30.00Z")

  "MonixFutureCache" should {
    "return nothing on empty cache" in {
      val cache = MonixFutureCache[String, Int]()
      cache.get(someKey).map(_ shouldBe None)
    }

    "store and retrieve value immediately" in {
      val timeProvider = TestTimeProvider.instance(someTime)
      val cache = MonixFutureCache[String, Int](timeProvider)
      for {
        _     <- cache.put(someKey, someValue, someTime.plusSeconds(60))
        value <- cache.get(someKey)
      } yield value shouldBe Some(someValue)
    }

    "return value right before expiration boundary" in {
      val timeProvider = TestTimeProvider.instance(someTime)
      val cache = MonixFutureCache[String, Int](timeProvider)
      for {
        _     <- cache.put(someKey, someValue, someTime.plusSeconds(60))
        _ = timeProvider.updateInstant(someTime.plusSeconds(60).minusNanos(1))
        value <- cache.get(someKey)
      } yield value shouldBe Some(someValue)
    }

    "not return value if expired" in {
      val timeProvider = TestTimeProvider.instance(someTime)
      val cache = MonixFutureCache[String, Int](timeProvider)
      for {
        _     <- cache.put(someKey, someValue, someTime.plusSeconds(60))
        _ = timeProvider.updateInstant(someTime.plusSeconds(60))
        value <- cache.get(someKey)
      } yield value shouldBe None
    }

    "remove value on expired get" in {
      val timeProvider = TestTimeProvider.instance(someTime)
      val cache = MonixFutureCache[String, Int](timeProvider)
      for {
        _      <- cache.put(someKey, someValue, someTime.plusSeconds(60))
        _ = timeProvider.updateInstant(someTime.plusSeconds(60))
        value1 <- cache.get(someKey) // this call should remove expired value from cache
        _ = timeProvider.updateInstant(someTime.plusSeconds(50)) // travel back in time, so in fact token is not expired yet
        value2 <- cache.get(someKey)
      } yield {
        value1 shouldBe None
        value2 shouldBe None
      }
    }
  }
}
