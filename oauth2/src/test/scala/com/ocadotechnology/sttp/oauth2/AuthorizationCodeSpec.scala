package com.ocadotechnology.sttp.oauth2

import cats.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.model.Uri
import AuthorizationCodeProvider.Config._

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
        Path(List("oauth2", "login").map(Segment)),
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
        Path(List("oauth2", "login").map(Segment)),
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
        Path(List("oauth2", "login").map(Segment)),
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
        Path(List("oauth2", "login").map(Segment)),
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
        Path(List("logout").map(Segment)),
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
        Path(List("logout").map(Segment)),
        List(
          ("client_id", clientId),
          ("redirect_uri", postLogoutUri.toString())
        )
      )
      val result = AuthorizationCode.logoutLink[TestEffect](baseUri, redirectUri, clientId, postLogoutUri.some).toString()

      result shouldEqual expected
    }

  }


}
