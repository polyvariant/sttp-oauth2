package com.ocadotechnology.sttp.oauth2

import cats.implicits._
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.common.Scope
import eu.timepit.refined.types.string.NonEmptyString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import sttp.client3._
import sttp.client3.testing.SttpBackendStub
import sttp.client3.testing._
import sttp.model.HeaderNames.Authorization
import sttp.model._

import scala.concurrent.duration._
import cats.MonadThrow
import scala.concurrent.Future

class SttpOauth2ClientCredentialsBackendSpec extends AsyncWordSpec with CrossPlatformAsyncTestSuite with Matchers {

  "SttpOauth2ClientCredentialsBackend" when {
    val tokenUrl: Uri = uri"https://authserver.org/oauth2/token"
    val clientId: NonEmptyString = NonEmptyString.unsafeFrom("clientid")
    val clientSecret: Secret[String] = Secret("secret")
    val scope: Scope = Scope.unsafeFrom("scope")
    val accessToken: Secret[String] = Secret("token")
    val accessTokenProvider = new TestAccessTokenProvider[Future](Map(Some(scope) -> accessToken))
    val testAppUrl: Uri = uri"https://testapp.org/test"

    "TestApp is invoked once" should {
      "request a token. add the token to the TestApp request" in {

        val mockBackend: SttpBackendStub[Future, Any] =
          SttpBackendStub
            .asynchronousFuture
            .whenTokenIsRequested()
            .thenRespond(Right(AccessTokenResponse(accessToken, Some("domain"), 100.seconds, Some(scope))))
            .whenTestAppIsRequestedWithToken(accessToken)
            .thenRespondOk()
        val backend = SttpOauth2ClientCredentialsBackend[Future, Any](accessTokenProvider)(Some(scope))(mockBackend)
        backend.send(basicRequest.get(testAppUrl).response(asStringAlways)).map(_.code shouldBe StatusCode.Ok)
      }
    }

    implicit class SttpBackendStubOps(
      val backend: SttpBackendStub[Future, Any]
    ) {
      import backend.WhenRequest

      def whenTokenIsRequested(
      ): WhenRequest = backend.whenRequestMatches { request =>
        request.method == Method.POST &&
        request.uri == tokenUrl &&
        request.forceBodyAsString == "grant_type=client_credentials&" +
          s"client_id=${clientId.value}&" +
          s"client_secret=${clientSecret.value}&" +
          s"scope=${scope.value}"
      }

      def whenTestAppIsRequestedWithToken(
        accessToken: Secret[String]
      ): WhenRequest = backend.whenRequestMatches { request =>
        request.method == Method.GET &&
        request.uri == testAppUrl &&
        request.headers.contains(Header(Authorization, s"Bearer ${accessToken.value}"))
      }
    }
  }

  private class TestAccessTokenProvider[F[_]: MonadThrow](
    tokens: Map[Option[Scope], Secret[String]]
  ) extends AccessTokenProvider[F] {

    def requestToken(
      scope: Option[Scope]
    ): F[ClientCredentialsToken.AccessTokenResponse] =
      tokens
        .get(scope)
        .map(secret => ClientCredentialsToken.AccessTokenResponse(secret, Some("domain"), 100.seconds, scope).pure[F])
        .getOrElse(MonadThrow[F].raiseError(new IllegalArgumentException(s"Unknown $scope")))

  }

}
