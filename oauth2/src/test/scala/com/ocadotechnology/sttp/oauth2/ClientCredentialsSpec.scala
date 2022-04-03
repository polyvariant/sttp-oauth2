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
import org.scalatest.TryValues
import org.scalatest.EitherValues
import sttp.model.StatusCode
import sttp.model.Method
import sttp.client3.Request

import scala.concurrent.duration._

class ClientCredentialsSpec extends AnyWordSpec with Matchers with TryValues with EitherValues {

  private val baseUri = Uri.unsafeParse("https://sso.example.com/")
  private val tokenUri = baseUri.withPath("token")
  private val tokenIntrospectUri = baseUri.withPath("token/introspect")
  private val clientSecret = Secret("secret")
  private val clientId: NonEmptyString = NonEmptyString.unsafeFrom("client-id")
  private val scope: Scope = Scope.unsafeFrom("scope")
  private val token = Secret("AX74aXyT")

  val oauth2Errors: List[(String, String, StatusCode, Error.OAuth2ErrorResponse.OAuth2ErrorResponseType)] = List(
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

    val requestToken = ClientCredentials.requestToken[Try](tokenUri, clientId, clientSecret, Some(scope))(_)

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
        scope = Scope.of("secondapp")
      )

    }

    oauth2Errors.foreach { case (errorKey, errorDescription, statusCode, error) =>
      s"support $errorKey OAuth2 error" in {

        val testingBackend = SttpBackendStub(TryMonad)
          .whenRequestMatches(validTokenRequest)
          .thenRespond(
            s"""
            {
            "error":"$errorKey",
            "error_description":"$errorDescription"
            }
            """,
            statusCode
          )

        requestToken(testingBackend).success.value.left.value shouldBe Error.OAuth2ErrorResponse(error, Some(errorDescription))
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

        val testingBackend = SttpBackendStub(TryMonad)
          .whenRequestMatches(validIntrospectRequest)
          .thenRespond(
            s"""
            {
            "error":"$errorKey",
            "error_description":"$errorDescription"
            }
            """,
            statusCode
          )

        introspectToken(testingBackend).success.value.left.value shouldBe Error.OAuth2ErrorResponse(error, Some(errorDescription))
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
