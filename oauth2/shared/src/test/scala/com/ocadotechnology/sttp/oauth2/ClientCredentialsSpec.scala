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
import sttp.client3.SttpBackend
import com.ocadotechnology.sttp.oauth2.codec.EntityDecoder

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

    def requestToken(
      backend: SttpBackend[Try, Any]
    )(
      implicit decoder: EntityDecoder[ClientCredentialsToken.AccessTokenResponse],
      errorDecoder: EntityDecoder[Error.OAuth2Error]
    ) = ClientCredentials.requestToken[Try](tokenUri, clientId, clientSecret, Some(scope))(backend)

    "successfully request token" in {
      val jsonResponse =
        """{
                  "access_token": "TAeJwlzT",
                  "domain": "mock",
                  "expires_in": 2399,
                  "scope": "secondapp",
                  "token_type": "Bearer"
              }"""

      val expectedDecodedResponse =
        ClientCredentialsToken.AccessTokenResponse(
          accessToken = Secret("TAeJwlzT"),
          domain = Some("mock"),
          expiresIn = 2399.seconds,
          scope = Scope.of("secondapp")
        )

      implicit val decoder: EntityDecoder[ClientCredentialsToken.AccessTokenResponse] = EntityDecoderMock.partialFunction {
        case `jsonResponse` => expectedDecodedResponse
      }

      implicit val errorDecoder: EntityDecoder[Error.OAuth2Error] = EntityDecoderMock.failing

      val testingBackend = SttpBackendStub(TryMonad)
        .whenRequestMatches(validTokenRequest)
        .thenRespond(
          jsonResponse,
          StatusCode.Ok
        )

      requestToken(testingBackend).success.value.value shouldBe expectedDecodedResponse

    }

    oauth2Errors.foreach { case (errorKey, errorDescription, statusCode, error) =>
      s"support $errorKey OAuth2 error" in {

        val jsonResponse =
          s"""
        {
        "error":"$errorKey",
        "error_description":"$errorDescription"
        }
        """

        val expectedDecodedResponse = Error.OAuth2ErrorResponse(error, Some(errorDescription))

        implicit val decoder: EntityDecoder[ClientCredentialsToken.AccessTokenResponse] = EntityDecoderMock.failing

        implicit val errorDecoder: EntityDecoder[Error.OAuth2Error] = EntityDecoderMock.partialFunction { case `jsonResponse` =>
          expectedDecodedResponse
        }

        val testingBackend = SttpBackendStub(TryMonad)
          .whenRequestMatches(validTokenRequest)
          .thenRespond(
            jsonResponse,
            statusCode
          )

        requestToken(testingBackend).success.value.left.value shouldBe expectedDecodedResponse
      }
    }

    "fail on unknown error" in {

      implicit val decoder: EntityDecoder[ClientCredentialsToken.AccessTokenResponse] = EntityDecoderMock.failing
      implicit val errorDecoder: EntityDecoder[Error.OAuth2Error] = EntityDecoderMock.failing

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

    def introspectToken(
      backend: SttpBackend[Try, Any]
    )(
      implicit decoder: EntityDecoder[Introspection.TokenIntrospectionResponse],
      errorDecoder: EntityDecoder[Error.OAuth2Error]
    ) = ClientCredentials.introspectToken[Try](tokenIntrospectUri, clientId, clientSecret, token)(backend)

    "successfully introspect token" in {
      val jsonResponse =
        s"""{
        "client_id": "$clientId",
        "active": true,
        "scope": "$scope"
      }"""

      val expectedDecodedResponse = Introspection.TokenIntrospectionResponse(
        active = true,
        clientId = Some(clientId.value),
        scope = Some(scope)
      )

      implicit val decoder: EntityDecoder[Introspection.TokenIntrospectionResponse] = EntityDecoderMock.partialFunction { `jsonResponse` =>
        expectedDecodedResponse
      }
      implicit val errorDecoder: EntityDecoder[Error.OAuth2Error] = EntityDecoderMock.failing

      val testingBackend = SttpBackendStub(TryMonad)
        .whenRequestMatches(validIntrospectRequest)
        .thenRespond(
          jsonResponse,
          StatusCode.Ok
        )

      introspectToken(testingBackend).success.value.value shouldBe expectedDecodedResponse

    }

    oauth2Errors.foreach { case (errorKey, errorDescription, statusCode, error) =>
      s"support $errorKey OAuth2 error" in {

        val jsonErrorResponse =
          s"""
          {
          "error":"$errorKey",
          "error_description":"$errorDescription"
          }
          """

        val expectedDecodedErrorResponse = Error.OAuth2ErrorResponse(error, Some(errorDescription))

        implicit val decoder: EntityDecoder[Introspection.TokenIntrospectionResponse] = EntityDecoderMock.failing
        implicit val errorDecoder: EntityDecoder[Error.OAuth2Error] = EntityDecoderMock.partialFunction { `jsonResponse` =>
          expectedDecodedErrorResponse
        }

        val testingBackend = SttpBackendStub(TryMonad)
          .whenRequestMatches(validIntrospectRequest)
          .thenRespond(
            jsonErrorResponse,
            statusCode
          )

        introspectToken(testingBackend).success.value.left.value shouldBe expectedDecodedErrorResponse
      }
    }

    "fail on unknown error" in {

      implicit val decoder: EntityDecoder[Introspection.TokenIntrospectionResponse] = EntityDecoderMock.failing
      implicit val errorDecoder: EntityDecoder[Error.OAuth2Error] = EntityDecoderMock.failing

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
