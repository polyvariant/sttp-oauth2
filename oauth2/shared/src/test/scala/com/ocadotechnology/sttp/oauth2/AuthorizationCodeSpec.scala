package com.ocadotechnology.sttp.oauth2

import cats.implicits._
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.model.Uri
import AuthorizationCodeProvider.Config._
import sttp.client4.testing._
import scala.util.Try
import sttp.monad.TryMonad
import scala.concurrent.duration.DurationInt

class AuthorizationCodeSpec extends AnyWordSpec with Matchers {

  type TestEffect[A] = cats.Id[A]

  private val baseUri = Uri.unsafeParse("https://sso.example.com/")
  private val extendedBaseUri = Uri.unsafeParse("https://sso.example.com/oauth999")
  private val redirectUri = Uri.unsafeParse("https://app.example.com/redirect")
  private val clientId = "client-id"

  "loginLink" should {

    "generate basic login link with default values" in {
      val expected = UriUtils.expectedResult(
        baseUri,
        Path(List("oauth2", "login").map(Segment.apply)),
        List(
          ("response_type", "code"),
          ("client_id", clientId),
          ("redirect_uri", redirectUri.toString()),
          ("state", ""),
          ("scope", "")
        )
      )
      val result = AuthorizationCode.loginLink[TestEffect](baseUri, redirectUri, clientId).toString()
      result shouldEqual expected
    }

    "ignore extra path elements in base uri" in {
      val expected = UriUtils.expectedResult(
        extendedBaseUri,
        Path(List("oauth2", "login").map(Segment.apply)),
        List(
          ("response_type", "code"),
          ("client_id", clientId),
          ("redirect_uri", redirectUri.toString()),
          ("state", ""),
          ("scope", "")
        )
      )
      val result = AuthorizationCode.loginLink[TestEffect](extendedBaseUri, redirectUri, clientId).toString()
      result shouldEqual expected
    }

    "generate login link with including state" in {
      val state = "CSRF_TOKEN_CONTENT"
      val expected = UriUtils.expectedResult(
        baseUri,
        Path(List("oauth2", "login").map(Segment.apply)),
        List(
          ("response_type", "code"),
          ("client_id", clientId),
          ("redirect_uri", redirectUri.toString()),
          ("state", state),
          ("scope", "")
        )
      )
      val result = AuthorizationCode.loginLink[TestEffect](baseUri, redirectUri, clientId, state = state.some).toString()
      result shouldEqual expected
    }

    "generate login link with including scopes" in {
      val rawScopes = List("users", "domains")
      val scopes = rawScopes.traverse(common.Scope.of).get.toSet
      val expected = UriUtils.expectedResult(
        baseUri,
        Path(List("oauth2", "login").map(Segment.apply)),
        List(
          ("response_type", "code"),
          ("client_id", clientId),
          ("redirect_uri", redirectUri.toString()),
          ("state", ""),
          ("scope", rawScopes.mkString("+"))
        )
      )
      val result = AuthorizationCode.loginLink[TestEffect](baseUri, redirectUri, clientId, scopes = scopes).toString()
      result shouldEqual expected
    }

  }

  "logoutLink" should {

    "generate basic logout link with default values" in {
      val expected = UriUtils.expectedResult(
        baseUri,
        Path(List("logout").map(Segment.apply)),
        List(
          ("client_id", clientId),
          ("redirect_uri", redirectUri.toString())
        )
      )
      val result = AuthorizationCode.logoutLink[TestEffect](baseUri, redirectUri, clientId).toString()

      result shouldEqual expected
    }

    "generate logout link respecting post logout uri" in {
      val postLogoutUri = Uri.unsafeParse("https://app.example.com/post-logout")
      val expected = UriUtils.expectedResult(
        baseUri,
        Path(List("logout").map(Segment.apply)),
        List(
          ("client_id", clientId),
          ("redirect_uri", postLogoutUri.toString())
        )
      )
      val result = AuthorizationCode.logoutLink[TestEffect](baseUri, redirectUri, clientId, postLogoutUri.some).toString()

      result shouldEqual expected
    }

  }

  "authCodeToToken" should {
    val tokenUri = baseUri.withPath("token")
    val redirectUri = Uri.unsafeParse("https://app.example.com/post-logout")
    val authCode = "auth-code-content"
    val clientSecret = Secret("secret")

    "decode valid extended response" in {
      val jsonResponse =
        // language=JSON
        """
        {
          "access_token": "123",
          "refresh_token": "456",
          "expires_in": 36000,
          "user_name": "testuser",
          "domain": "somedomain",
          "user_details": {
            "username": "",
            "name": "",
            "forename": "",
            "surname": "",
            "mail": "",
            "cn": "",
            "sn": ""
          },
          "roles": [],
          "scope": "",
          "security_level": 0,
          "user_id": "",
          "token_type": ""
        }
        """

      val testingBackend = BackendStub(TryMonad)
        .whenRequestMatches(_ => true)
        .thenRespond(jsonResponse)

      implicit val decoder: JsonDecoder[ExtendedOAuth2TokenResponse] = JsonDecoderMock.partialFunction { case `jsonResponse` =>
        ExtendedOAuth2TokenResponse(
          Secret("secret"),
          "refreshToken",
          30.seconds,
          "userName",
          "domain",
          TokenUserDetails(
            "username",
            "name",
            "forename",
            "surname",
            "mail",
            "cn",
            "sn"
          ),
          roles = Set(),
          "scope",
          securityLevel = 2L,
          "userId",
          "tokenType"
        )
      }

      val response = AuthorizationCode.authCodeToToken[Try, ExtendedOAuth2TokenResponse](
        tokenUri,
        redirectUri,
        clientId,
        clientSecret,
        authCode
      )(testingBackend)
      response.isSuccess shouldBe true
    }

    "decode valid basic response" in {
      val jsonResponse = """{"access_token":"gho_16C7e42F292c6912E7710c838347Ae178B4a", "scope":"repo,gist", "token_type":"bearer"}"""

      implicit val decoder: JsonDecoder[OAuth2TokenResponse] = JsonDecoderMock.partialFunction { case `jsonResponse` =>
        OAuth2TokenResponse(
          Secret("secret"),
          "scope",
          "token_type",
          expiresIn = None,
          refreshToken = None
        )
      }

      val testingBackend = BackendStub(TryMonad)
        .whenRequestMatches(_ => true)
        .thenRespond(jsonResponse)
      val response = AuthorizationCode.authCodeToToken[Try, OAuth2TokenResponse](
        tokenUri,
        redirectUri,
        clientId,
        clientSecret,
        authCode
      )(testingBackend)
      response.isSuccess shouldBe true
    }

    "fail effect with circe error on decode error" in {
      implicit val decoder: JsonDecoder[OAuth2TokenResponse] = JsonDecoderMock.failing

      val testingBackend = BackendStub(TryMonad)
        .whenRequestMatches(_ => true)
        .thenRespond("{}")
      val response = AuthorizationCode.authCodeToToken[Try, OAuth2TokenResponse](
        tokenUri,
        redirectUri,
        clientId,
        clientSecret,
        authCode
      )(testingBackend)
      response.toEither shouldBe a[Left[JsonDecoder.Error, _]]
    }

    "fail effect with runtime error on all other errors" in {
      implicit val decoder: JsonDecoder[OAuth2TokenResponse] = JsonDecoderMock.failing

      val testingBackend = BackendStub(TryMonad)
        .whenRequestMatches(_ => true)
        .thenRespondServerError()
      val response = AuthorizationCode.authCodeToToken[Try, OAuth2TokenResponse](
        tokenUri,
        redirectUri,
        clientId,
        clientSecret,
        authCode
      )(testingBackend)
      response.toEither shouldBe a[Left[RuntimeException, _]]
    }

  }

}
