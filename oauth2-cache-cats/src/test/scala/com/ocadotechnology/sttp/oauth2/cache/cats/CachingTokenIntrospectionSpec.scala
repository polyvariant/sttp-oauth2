package com.ocadotechnology.sttp.oauth2.cache.cats

import cats.Functor
import cats.effect.IO
import cats.effect.Ref
import cats.effect.kernel.Clock
import cats.effect.kernel.Outcome.Succeeded
import cats.effect.testkit.TestContext
import cats.effect.testkit.TestInstances
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.TokenIntrospection
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant
import scala.concurrent.duration._

class CachingTokenIntrospectionSpec extends AnyWordSpec with Matchers with TestInstances {
  private implicit val ticker: Ticker = Ticker(TestContext())

  private val testToken: Secret[String] = Secret("secret")

  private def testToken60Seconds(now: Instant): TokenIntrospectionResponse = TokenIntrospectionResponse(
    active = true,
    exp = Some(now.plusSeconds(60))
  )

  private def testToken5Seconds(now: Instant): TokenIntrospectionResponse = TokenIntrospectionResponse(
    active = true,
    exp = Some(now.plusSeconds(5))
  )

  private def testToken2Seconds(now: Instant): TokenIntrospectionResponse = TokenIntrospectionResponse(
    active = true,
    exp = Some(now.plusSeconds(2))
  )

  private val inactiveToken: TokenIntrospectionResponse = TokenIntrospectionResponse(active = false)

  private val testFailedResponse: TokenIntrospectionResponse = TokenIntrospectionResponse(active = false)

  "CachingTokenIntrospection" should {
    "delegate token retrieval on first call" in runTest { case (delegate, cachingIntrospection) =>
      for {
        now    <- Clock[IO].realTimeInstant
        _      <- delegate.updateTokenResponse(testToken, testToken60Seconds(now))
        result <- cachingIntrospection.introspect(testToken)
      } yield result shouldBe testToken60Seconds(now)
    }

    "return cached response if it's not yet expired" in runTest { case (delegate, cachingIntrospection) =>
      for {
        now    <- Clock[IO].realTimeInstant
        _      <- delegate.updateTokenResponse(testToken, testToken60Seconds(now))
        _      <- cachingIntrospection.introspect(testToken)
        _      <- Clock[IO].sleep(3.seconds)
        _      <- delegate.updateTokenResponse(testToken, testToken5Seconds(now))
        result <- cachingIntrospection.introspect(testToken)
      } yield result shouldBe testToken60Seconds(now)
    }

    "fetch the new introspection result if the cache has expired" in runTest { case (delegate, cachingIntrospection) =>
      for {
        now    <- Clock[IO].realTimeInstant
        _      <- delegate.updateTokenResponse(testToken, testToken2Seconds(now))
        _      <- cachingIntrospection.introspect(testToken)
        _      <- Clock[IO].sleep(3.seconds)
        _      <- delegate.updateTokenResponse(testToken, inactiveToken)
        result <- cachingIntrospection.introspect(testToken)
      } yield result shouldBe inactiveToken
    }

  }

  def runTest(test: ((TestTokenIntrospection[IO], CachingTokenIntrospection[IO])) => IO[Assertion]): Assertion =
    unsafeRun(prepareTest.flatMap(test)) match {
      case Succeeded(Some(assertion)) => assertion
      case wrongResult                => fail(s"Test should finish successfully. Instead ended with $wrongResult")
    }

  private def prepareTest: IO[(TestTokenIntrospection[IO], CachingTokenIntrospection[IO])] =
    for {
      state <- Ref.of[IO, Map[Secret[String], TokenIntrospectionResponse]](Map.empty)
      cache <- CatsRefExpiringCache[IO, Secret[String], TokenIntrospectionResponse]
      delegate = TestTokenIntrospection(state)
      cachingIntrospection = CachingTokenIntrospection[IO](delegate, cache, 5.seconds)
    } yield (delegate, cachingIntrospection)

  trait TestTokenIntrospection[F[_]] extends TokenIntrospection[F] {
    def introspect(token: Secret[String]): F[TokenIntrospectionResponse]
    def updateTokenResponse(token: Secret[String], response: TokenIntrospectionResponse): F[Unit]
  }

  object TestTokenIntrospection {

    def apply[F[_]: Functor](ref: Ref[F, Map[Secret[String], TokenIntrospectionResponse]]): TestTokenIntrospection[F] =
      new TestTokenIntrospection[F] {
        def introspect(token: Secret[String]): F[TokenIntrospectionResponse] =
          ref.get.map(_.get(token).getOrElse(testFailedResponse))

        def updateTokenResponse(token: Secret[String], response: TokenIntrospectionResponse): F[Unit] =
          ref.update(state => state + (token -> response))
      }

  }

}
