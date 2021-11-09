package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common.Scope
import com.ocadotechnology.sttp.oauth2.common.Error
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import sttp.model.Uri
import sttp.client3.testing._
import sttp.monad.TryMonad

import scala.util.Try
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.auto._
import org.scalatest.TryValues
import org.scalatest.EitherValues
import sttp.model.StatusCode
import sttp.model.Method
import sttp.client3.{HttpError, Request}

import scala.concurrent.duration._

class ClientCredentialsSpec extends AnyWordSpec with Matchers with TryValues with EitherValues {

  val baseUri = Uri.unsafeParse("https://sso.example.com/")
  val tokenUri = baseUri.withPath("token")
  val tokenIntrospectUri = baseUri.withPath("token/introspect")
  val clientSecret = Secret("secret")
  val clientId: NonEmptyString = "client-id"
  val scope: Scope = "scope"
  val token = Secret("AX74aXyT")

  val oauth2Errors = List(
    ("invalid_client", "Client is missing or invalid.", StatusCode.Unauthorized, Error.OAuth2ErrorResponse.InvalidClient),
    ("invalid_request", "Unsupported parameter.", StatusCode.BadRequest, Error.OAuth2ErrorResponse.InvalidRequest),
    ("invalid_grant", "Grant is invalid.", StatusCode.BadRequest, Error.OAuth2ErrorResponse.InvalidGrant),
    (
      "unauthorized_client",
      "Client is unaothorized for this grant.",
      StatusCode.BadRequest,
      Error.OAuth2ErrorResponse.UnauthorizedClient
    ),
    ("unsupported_grant_type", "Grant is unsupported.", StatusCode.BadRequest, Error.OAuth2ErrorResponse.UnsupportedGrantType),
    ("invalid_scope", "Scope is invalid.", StatusCode.BadRequest, Error.OAuth2ErrorResponse.InvalidScope)
  )

  "ClientCredentials.requestToken" should {

    val requestToken = ClientCredentials.requestToken[Try](tokenUri, clientId, clientSecret, scope)(_)

    "successfully request token" in {
      val testingBackend = SttpBackendStub(TryMonad)
        .whenRequestMatches(validTokenRequest)
        .thenRespond(
          """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "scope": "secondapp",
            "token_type": "Bearer"
        }""",
          StatusCode.Ok
        )

      requestToken(testingBackend).success.value.value shouldBe ClientCredentialsToken.AccessTokenResponse(
        accessToken = Secret("TAeJwlzT"),
        domain = Some("mock"),
        expiresIn = 2399.seconds,
        scope = Scope.refine("secondapp")
      )

    }

    oauth2Errors.foreach { case (errorKey, errorDescription, statusCode, error) =>
      s"support $errorKey OAuth2 error" in {
        val body = s"""
                  |{
                  |"error":"$errorKey",
                  |"error_description":"$errorDescription"
                  |}
                  |""".stripMargin

        val testingBackend = SttpBackendStub(TryMonad)
          .whenRequestMatches(validTokenRequest)
          .thenRespond(body, statusCode)

        requestToken(testingBackend).success.value.left.value shouldBe
          Error.OAuth2ErrorResponse(error, Some(errorDescription), HttpError(body, statusCode))
      }
    }

    "fail on unknown error" in {

      val testingBackend = SttpBackendStub(TryMonad)
        .whenRequestMatches(validTokenRequest)
        .thenRespond("""Unknown error""", StatusCode.InternalServerError)

      requestToken(testingBackend).success.value.left.value shouldBe a[Error.HttpClientError]
    }

    def validTokenRequest(request: Request[_, _]): Boolean =
      request.method == Method.POST &&
        request.uri == tokenUri &&
        request.forceBodyAsString == "grant_type=client_credentials&" +
        s"client_id=${clientId.value}&" +
        s"client_secret=${clientSecret.value}&" +
        s"scope=${scope.value}"
  }

  "ClientCredentials.introspectToken" should {

    val introspectToken = ClientCredentials.introspectToken[Try](tokenIntrospectUri, clientId, clientSecret, token)(_)

    "successfully introspect token" in {
      val testingBackend = SttpBackendStub(TryMonad)
        .whenRequestMatches(validIntrospectRequest)
        .thenRespond(
          s"""{
            "client_id": "$clientId",
            "active": true,
            "scope": "$scope"
          }""",
          StatusCode.Ok
        )

      introspectToken(testingBackend).success.value.value shouldBe Introspection.TokenIntrospectionResponse(
        active = true,
        clientId = Some(clientId.value),
        scope = Some(scope)
      )

    }

    oauth2Errors.foreach { case (errorKey, errorDescription, statusCode, error) =>
      s"support $errorKey OAuth2 error" in {
        val body = s"""
            |{
            |"error":"$errorKey",
            |"error_description":"$errorDescription"
            |}
            |""".stripMargin

        val testingBackend = SttpBackendStub(TryMonad)
          .whenRequestMatches(validIntrospectRequest)
          .thenRespond(body, statusCode)

        introspectToken(testingBackend).success.value.left.value shouldBe
          Error.OAuth2ErrorResponse(error, Some(errorDescription), HttpError(body, statusCode))
      }
    }

    "fail on unknown error" in {

      val testingBackend = SttpBackendStub(TryMonad)
        .whenRequestMatches(validIntrospectRequest)
        .thenRespond("""Unknown error""", StatusCode.InternalServerError)

      introspectToken(testingBackend).success.value.left.value shouldBe a[Error.HttpClientError]
    }

    def validIntrospectRequest(request: Request[_, _]): Boolean =
      request.method == Method.POST &&
        request.uri == tokenIntrospectUri &&
        request.forceBodyAsString == s"client_id=${clientId.value}&" +
        s"client_secret=${clientSecret.value}&" +
        s"token=${token.value}"
  }

}
