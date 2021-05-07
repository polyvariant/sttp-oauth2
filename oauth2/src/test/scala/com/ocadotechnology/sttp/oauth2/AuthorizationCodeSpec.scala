package com.ocadotechnology.sttp.oauth2

import cats.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.model.Uri

class AuthorizationCodeSpec extends AnyWordSpec with Matchers {

  type TestEffect[A] = cats.Id[A]

  private val baseUri = Uri.unsafeParse("https://sso.example.com/oauth2")
  private val redirectUri = Uri.unsafeParse("https://app.example.com/redirect")
  private val clientId = "client-id"

  "loginLink" should {

    "generate basic login link with default values" in {
      val expected = baseUri
        .addPath("login")
        .addParam("response_type", "code")
        .addParam("client_id", clientId)
        .addParam("redirect_uri", redirectUri.toString())
        .addParam("state", "")
        .addParam("scope", "")
      val result = AuthorizationCode.loginLink[TestEffect](baseUri, redirectUri, clientId)
      result shouldEqual expected
    }

    "generate login link with including state" in {
      val state = "CSRF_TOKEN_CONTENT"
      val expected = baseUri
        .addPath("login")
        .addParam("response_type", "code")
        .addParam("client_id", clientId)
        .addParam("redirect_uri", redirectUri.toString())
        .addParam("state", state)
        .addParam("scope", "")
      val result = AuthorizationCode.loginLink[TestEffect](baseUri, redirectUri, clientId, state = state.some)
      result shouldEqual expected
    }

    "generate login link with including scopes" in {
      val rawScopes = List("users", "domains")
      val scopes = rawScopes.traverse(common.Scope.of).get.toSet
      val expected = baseUri
        .addPath("login")
        .addParam("response_type", "code")
        .addParam("client_id", clientId)
        .addParam("redirect_uri", redirectUri.toString())
        .addParam("state", "")
        .addParam("scope", rawScopes.mkString(" "))
      val result = AuthorizationCode.loginLink[TestEffect](baseUri, redirectUri, clientId, scopes = scopes)
      result shouldEqual expected
    }

  }

  "logoutLink" should {

    "generate basic logout link with default values" in {
      val expected = baseUri
        .withPath("logout")
        .addParam("client_id", clientId)
        .addParam("redirect_uri", redirectUri.toString())
      val result = AuthorizationCode.logoutLink[TestEffect](baseUri, redirectUri, clientId)

      result shouldEqual expected
    }

    "generate logout link respecting post logout uri" in {
      val postLogoutUri = Uri.unsafeParse("https://app.example.com/post-logout")
      val expected = baseUri
        .withPath("logout")
        .addParam("client_id", clientId)
        .addParam("redirect_uri", postLogoutUri.toString())
      val result = AuthorizationCode.logoutLink[TestEffect](baseUri, redirectUri, clientId, postLogoutUri.some)

      result shouldEqual expected
    }

  }


}
