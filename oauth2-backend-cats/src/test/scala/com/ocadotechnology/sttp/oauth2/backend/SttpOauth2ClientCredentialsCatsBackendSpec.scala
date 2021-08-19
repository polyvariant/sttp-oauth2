package com.ocadotechnology.sttp.oauth2.backend

import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Timer
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.common.Scope
import eu.timepit.refined.types.string.NonEmptyString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import sttp.client3._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.testing.SttpBackendStub
import sttp.client3.testing._
import sttp.model.HeaderNames.Authorization
import sttp.model._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class SttpOauth2ClientCredentialsCatsBackendSpec extends AsyncWordSpec with Matchers {
  implicit override val executionContext: ExecutionContext = ExecutionContext.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(executionContext)
  implicit val timer: Timer[IO] = IO.timer(executionContext)

  "SttpOauth2ClientCredentialsBackend" when {
    val tokenUrl: Uri = uri"https://authserver.org/oauth2/token"
    val clientId: NonEmptyString = NonEmptyString("clientid")
    val clientSecret: Secret[String] = Secret("secret")
    val scope: Scope = Scope.refine("scope")

    val testAppUrl: Uri = uri"https://testapp.org/test"

    "TestApp is invoked once" should {
      "request a token. add the token to the TestApp request" in {
        val accessToken: Secret[String] = Secret("token")
        implicit val mockBackend: SttpBackendStub[IO, Any] = AsyncHttpClientCatsBackend
          .stub[IO]
          .whenTokenIsRequested()
          .thenRespond(Right(AccessTokenResponse(accessToken, Some("domain"), 100.seconds, scope)))
          .whenTestAppIsRequestedWithToken(accessToken)
          .thenRespondOk()

        for {
          backend  <- SttpOauth2ClientCredentialsCatsBackend[IO, Any](tokenUrl, uri"https://unused", clientId, clientSecret)(scope)
          response <- backend.send(basicRequest.get(testAppUrl).response(asStringAlways))
        } yield response.code shouldBe StatusCode.Ok
      }.unsafeToFuture()
    }

    "TestApp is invoked twice sequentially" should {
      "first invocation is requesting a token, second invocation is getting the token from the cache. add the token to the both TestApp requests" in {
        val accessToken: Secret[String] = Secret("token")
        implicit val recordingMockBackend: RecordingSttpBackend[IO, Any] = new RecordingSttpBackend(
          AsyncHttpClientCatsBackend
            .stub[IO]
            .whenTokenIsRequested()
            .thenRespond(Right(AccessTokenResponse(accessToken, Some("domain"), 100.seconds, scope)))
            .whenTestAppIsRequestedWithToken(accessToken)
            .thenRespondOk()
        )

        for {
          backend   <- SttpOauth2ClientCredentialsCatsBackend[IO, Any](tokenUrl, uri"https://unused", clientId, clientSecret)(scope)
          invokeTestApp = backend.send(basicRequest.get(testAppUrl).response(asStringAlways))
          response1 <- invokeTestApp
          response2 <- invokeTestApp
        } yield {
          response1.code shouldBe StatusCode.Ok
          response2.code shouldBe StatusCode.Ok
          recordingMockBackend.invocationCountByUri shouldBe Map(tokenUrl -> 1, testAppUrl -> 2)
        }
      }.unsafeToFuture()
    }

    "TestApp is invoked twice in parallel" should {
      "first invocation is requesting a token, second invocation is waiting for token response and getting the token from the cache. add the token to the both TestApp requests" in {
        val accessToken: Secret[String] = Secret("token")
        implicit val recordingMockBackend: RecordingSttpBackend[IO, Any] = new RecordingSttpBackend(
          AsyncHttpClientCatsBackend
            .stub[IO]
            .whenTokenIsRequested()
            .thenRespondF(IO.sleep(200.millis).as(Response.ok(Right(AccessTokenResponse(accessToken, Some("domain"), 100.seconds, scope)))))
            .whenTestAppIsRequestedWithToken(accessToken)
            .thenRespondOk()
        )

        for {
          backend <- SttpOauth2ClientCredentialsCatsBackend[IO, Any](tokenUrl, uri"https://unused", clientId, clientSecret)(scope)
          invokeTestApp = backend.send(basicRequest.get(testAppUrl).response(asStringAlways))
          (response1, response2) <- (invokeTestApp, invokeTestApp).parTupled
        } yield {
          response1.code shouldBe StatusCode.Ok
          response2.code shouldBe StatusCode.Ok
          recordingMockBackend.invocationCountByUri shouldBe Map(tokenUrl -> 1, testAppUrl -> 2)
        }
      }.unsafeToFuture()
    }

    "TestApp is invoked after token expires" should {
      "first invocation is requesting a token, second invocation is requesting a token, because the previous token is expired. add the token to the both TestApp requests" in {
        val accessToken1: Secret[String] = Secret("token1")
        val accessToken2: Secret[String] = Secret("token2")
        implicit val recordingMockBackend: RecordingSttpBackend[IO, Any] = new RecordingSttpBackend(
          AsyncHttpClientCatsBackend
            .stub[IO]
            .whenTokenIsRequested()
            .thenRespondCyclic(
              Right(AccessTokenResponse(accessToken1, Some("domain"), 100.millis, scope)),
              Right(AccessTokenResponse(accessToken2, Some("domain"), 100.millis, scope))
            )
            .whenTestAppIsRequestedWithToken(accessToken1)
            .thenRespond("body1")
            .whenTestAppIsRequestedWithToken(accessToken2)
            .thenRespond("body2")
        )

        for {
          backend   <- SttpOauth2ClientCredentialsCatsBackend[IO, Any](tokenUrl, uri"https://unused", clientId, clientSecret)(scope)
          invokeTestApp = backend.send(basicRequest.get(testAppUrl).response(asStringAlways))
          response1 <- invokeTestApp
          _         <- IO.sleep(200.millis)
          response2 <- invokeTestApp
        } yield {
          response1.code shouldBe StatusCode.Ok
          response1.body shouldBe "body1"
          response2.code shouldBe StatusCode.Ok
          response2.body shouldBe "body2"
          recordingMockBackend.invocationCountByUri shouldBe Map(tokenUrl -> 2, testAppUrl -> 2)
        }
      }.unsafeToFuture()
    }

    implicit class SttpBackendStubOps[F[_], P](val backend: SttpBackendStub[F, P]) {
      import backend.WhenRequest

      def whenTokenIsRequested(): WhenRequest = backend.whenRequestMatches { request =>
        request.method == Method.POST &&
        request.uri == tokenUrl &&
        request.forceBodyAsString == "grant_type=client_credentials&" +
          s"client_id=${clientId.value}&" +
          s"client_secret=${clientSecret.value}&" +
          s"scope=${scope.value}"
      }

      def whenTestAppIsRequestedWithToken(accessToken: Secret[String]): WhenRequest = backend.whenRequestMatches { request =>
        request.method == Method.GET &&
        request.uri == testAppUrl &&
        request.headers.contains(Header(Authorization, s"Bearer ${accessToken.value}"))
      }
    }

    implicit class RecordingSttpBackendOps[F[_], P](backend: RecordingSttpBackend[F, P]) {
      def invocationCountByUri: Map[Uri, Int] = backend.allInteractions.groupBy(_._1.uri).fmap(_.size)
    }
  }

}
