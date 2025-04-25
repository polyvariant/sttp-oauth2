package org.polyvariant.sttp.oauth2

import org.polyvariant.sttp.oauth2.common.Error
import org.polyvariant.sttp.oauth2.common.Scope
import org.polyvariant.sttp.oauth2.json.JsonDecoder
import eu.timepit.refined.types.string.NonEmptyString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.EitherValues
import org.scalatest.TryValues
import sttp.client4.testing._
import sttp.client4.GenericRequest
import sttp.client4.GenericBackend
import sttp.model.Method
import sttp.model.StatusCode
import sttp.model.Uri
import sttp.monad.TryMonad

import scala.concurrent.duration._
import scala.util.Try

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
      backend: GenericBackend[Try, Any]
    )(
      implicit decoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse],
      errorDecoder: JsonDecoder[Error.OAuth2Error]
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

      implicit val decoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse] = JsonDecoderMock.partialFunction {
        case `jsonResponse` => expectedDecodedResponse
      }

      implicit val errorDecoder: JsonDecoder[Error.OAuth2Error] = JsonDecoderMock.failing

      val testingBackend = BackendStub(TryMonad)
        .whenRequestMatches(validTokenRequest)
        .thenRespondAdjust(
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

        implicit val decoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse] = JsonDecoderMock.failing

        implicit val errorDecoder: JsonDecoder[Error.OAuth2Error] = JsonDecoderMock.partialFunction { case `jsonResponse` =>
          expectedDecodedResponse
        }

        val testingBackend = BackendStub(TryMonad)
          .whenRequestMatches(validTokenRequest)
          .thenRespondAdjust(
            jsonResponse,
            statusCode
          )

        requestToken(testingBackend).success.value.left.value shouldBe expectedDecodedResponse
      }
    }

    "fail on unknown error" in {

      implicit val decoder: JsonDecoder[ClientCredentialsToken.AccessTokenResponse] = JsonDecoderMock.failing
      implicit val errorDecoder: JsonDecoder[Error.OAuth2Error] = JsonDecoderMock.failing

      val testingBackend = BackendStub(TryMonad)
        .whenRequestMatches(validTokenRequest)
        .thenRespondAdjust("""Unknown error""", StatusCode.InternalServerError)

      requestToken(testingBackend).success.value.left.value shouldBe a[Error.HttpClientError]
    }

    def validTokenRequest(request: GenericRequest[_, _]): Boolean =
      request.method == Method.POST &&
        request.uri == tokenUri &&
        request.forceBodyAsString == "grant_type=client_credentials&" +
        s"client_id=${clientId.value}&" +
        s"client_secret=${clientSecret.value}&" +
        s"scope=${scope.value}"
  }

  "ClientCredentials.introspectToken" should {

    def introspectToken(
      backend: GenericBackend[Try, Any]
    )(
      implicit decoder: JsonDecoder[Introspection.TokenIntrospectionResponse],
      errorDecoder: JsonDecoder[Error.OAuth2Error]
    ) = ClientCredentials.introspectToken[Try](tokenIntrospectUri, clientId, clientSecret, token)(backend)

    "successfully introspect token" in {
      val jsonResponse =
        // language=JSON
        s"""
        {
          "client_id": "$clientId",
          "active": true,
          "scope": "$scope"
        }
        """

      val expectedDecodedResponse = Introspection.TokenIntrospectionResponse(
        active = true,
        clientId = Some(clientId.value),
        scope = Some(scope)
      )

      implicit val decoder: JsonDecoder[Introspection.TokenIntrospectionResponse] = JsonDecoderMock.partialFunction { case `jsonResponse` =>
        expectedDecodedResponse
      }
      implicit val errorDecoder: JsonDecoder[Error.OAuth2Error] = JsonDecoderMock.failing

      val testingBackend = BackendStub(TryMonad)
        .whenRequestMatches(validIntrospectRequest)
        .thenRespondAdjust(
          jsonResponse,
          StatusCode.Ok
        )

      introspectToken(testingBackend).success.value.value shouldBe expectedDecodedResponse

    }

    oauth2Errors.foreach { case (errorKey, errorDescription, statusCode, error) =>
      s"support $errorKey OAuth2 error" in {

        val jsonErrorResponse =
          // language=JSON
          s"""
          {
            "error":"$errorKey",
            "error_description":"$errorDescription"
          }
          """

        val expectedDecodedErrorResponse = Error.OAuth2ErrorResponse(error, Some(errorDescription))

        implicit val decoder: JsonDecoder[Introspection.TokenIntrospectionResponse] = JsonDecoderMock.failing
        implicit val errorDecoder: JsonDecoder[Error.OAuth2Error] = JsonDecoderMock.partialFunction { case `jsonErrorResponse` =>
          expectedDecodedErrorResponse
        }

        val testingBackend = BackendStub(TryMonad)
          .whenRequestMatches(validIntrospectRequest)
          .thenRespondAdjust(
            jsonErrorResponse,
            statusCode
          )

        introspectToken(testingBackend).success.value.left.value shouldBe expectedDecodedErrorResponse
      }
    }

    "fail on unknown error" in {

      implicit val decoder: JsonDecoder[Introspection.TokenIntrospectionResponse] = JsonDecoderMock.failing
      implicit val errorDecoder: JsonDecoder[Error.OAuth2Error] = JsonDecoderMock.failing

      val testingBackend = BackendStub(TryMonad)
        .whenRequestMatches(validIntrospectRequest)
        .thenRespondAdjust("""Unknown error""", StatusCode.InternalServerError)

      introspectToken(testingBackend).success.value.left.value shouldBe a[Error.HttpClientError]
    }

    def validIntrospectRequest(request: GenericRequest[_, _]): Boolean =
      request.method == Method.POST &&
        request.uri == tokenIntrospectUri &&
        request.forceBodyAsString == s"client_id=${clientId.value}&" +
        s"client_secret=${clientSecret.value}&" +
        s"token=${token.value}"
  }

}
