package com.ocadotechnology.sttp.oauth2.backend

import cats.implicits._
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.common.Scope
import eu.timepit.refined.types.string.NonEmptyString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import sttp.client3._
import sttp.client3.asynchttpclient.future.AsyncHttpClientFutureBackend
import sttp.client3.testing.SttpBackendStub
import sttp.client3.testing._
import sttp.model.HeaderNames.Authorization
import sttp.model._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

class SttpOauth2ClientCredentialsFutureBackendSpec extends AsyncWordSpec with Matchers {
  implicit override val executionContext: ExecutionContext = ExecutionContext.global

  "SttpOauth2ClientCredentialsBackend" when {
    val tokenUrl: Uri = uri"https://authserver.org/oauth2/token"
    val clientId: NonEmptyString = NonEmptyString("clientid")
    val clientSecret: Secret[String] = Secret("secret")
    val scope: Scope = Scope.refine("scope")

    val testAppUrl: Uri = uri"https://testapp.org/test"

    "TestApp is invoked once" should {
      "request a token. add the token to the TestApp request" in {
        val accessToken: Secret[String] = Secret("token")
        implicit val mockBackend: SttpBackendStub[Future, Any] = AsyncHttpClientFutureBackend
          .stub
          .whenTokenIsRequested()
          .thenRespond(Right(AccessTokenResponse(accessToken, Some("domain"), 100.seconds, scope)))
          .whenTestAppIsRequestedWithToken(accessToken)
          .thenRespondOk()

        val backend = SttpOauth2ClientCredentialsFutureBackend(tokenUrl, uri"https://unused", clientId, clientSecret)(scope)

        backend.send(basicRequest.get(testAppUrl).response(asStringAlways)).map(_.code shouldBe StatusCode.Ok)
      }
    }

    "TestApp is invoked twice sequentially" should {
      "first invocation is requesting a token, second invocation is getting the token from the cache. add the token to the both TestApp requests" in {
        val accessToken: Secret[String] = Secret("token")
        implicit val recordingMockBackend: RecordingSttpBackend[Future, Any] = new RecordingSttpBackend(
          AsyncHttpClientFutureBackend
            .stub
            .whenTokenIsRequested()
            .thenRespond(Right(AccessTokenResponse(accessToken, Some("domain"), 100.seconds, scope)))
            .whenTestAppIsRequestedWithToken(accessToken)
            .thenRespondOk()
        )

        val backend = SttpOauth2ClientCredentialsFutureBackend(tokenUrl, uri"https://unused", clientId, clientSecret)(scope)
        def invokeTestApp: Future[Response[String]] = backend.send(basicRequest.get(testAppUrl).response(asStringAlways))

        for {
          response1 <- invokeTestApp
          response2 <- invokeTestApp
        } yield {
          response1.code shouldBe StatusCode.Ok
          response2.code shouldBe StatusCode.Ok
          recordingMockBackend.invocationCountByUri shouldBe Map(tokenUrl -> 1, testAppUrl -> 2)
        }
      }
    }

    "TestApp is invoked twice in parallel" should {
      "first invocation is requesting a token, second invocation is waiting for token response and getting the token from the cache. add the token to the both TestApp requests" in {
        val accessToken: Secret[String] = Secret("token")
        implicit val recordingMockBackend: RecordingSttpBackend[Future, Any] = new RecordingSttpBackend(
          AsyncHttpClientFutureBackend
            .stub
            .whenTokenIsRequested()
            .thenRespondF(Future(Thread.sleep(200)).as(Response.ok(Right(AccessTokenResponse(accessToken, Some("domain"), 100.seconds, scope)))))
            .whenTestAppIsRequestedWithToken(accessToken)
            .thenRespondOk()
        )

        val backend = SttpOauth2ClientCredentialsFutureBackend(tokenUrl, uri"https://unused", clientId, clientSecret)(scope)
        def invokeTestApp: Future[Response[String]] = backend.send(basicRequest.get(testAppUrl).response(asStringAlways))

        val process1 = invokeTestApp
        val process2 = invokeTestApp
        process1.zipWith(process2) { (response1, response2) =>
          response1.code shouldBe StatusCode.Ok
          response2.code shouldBe StatusCode.Ok
          recordingMockBackend.invocationCountByUri shouldBe Map(tokenUrl -> 1, testAppUrl -> 2)
        }
      }
    }

    "TestApp is invoked after token expires" should {
      "first invocation is requesting a token, second invocation is requesting a token, because the previous token is expired. add the token to the both TestApp requests" in {
        val accessToken1: Secret[String] = Secret("token1")
        val accessToken2: Secret[String] = Secret("token2")
        implicit val recordingMockBackend: RecordingSttpBackend[Future, Any] = new RecordingSttpBackend(
          AsyncHttpClientFutureBackend
            .stub
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

        val backend = SttpOauth2ClientCredentialsFutureBackend(tokenUrl, uri"https://unused", clientId, clientSecret)(scope)
        def invokeTestApp: Future[Response[String]] = backend.send(basicRequest.get(testAppUrl).response(asStringAlways))

        for {
          response1 <- invokeTestApp
          _         <- Future(Thread.sleep(200))
          response2 <- invokeTestApp
        } yield {
          response1.code shouldBe StatusCode.Ok
          response1.body shouldBe "body1"
          response2.code shouldBe StatusCode.Ok
          response2.body shouldBe "body2"
          recordingMockBackend.invocationCountByUri shouldBe Map(tokenUrl -> 2, testAppUrl -> 2)
        }
      }
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
