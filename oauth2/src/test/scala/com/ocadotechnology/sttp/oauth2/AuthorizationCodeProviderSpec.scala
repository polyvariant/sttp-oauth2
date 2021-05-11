package com.ocadotechnology.sttp.oauth2

import cats.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.model.Uri
import sttp.client3.SttpBackend
import sttp.client3.testing.SttpBackendStub
import AuthorizationCodeProvider.Config._

class AuthorizationCodeProviderSpec extends AnyWordSpec with Matchers {

  type TestEffect[A] = cats.Id[A]

  private val baseUri = Uri.unsafeParse("https://sso.example.com/oauth2")
  private val redirectUri = Uri.unsafeParse("https://app.example.com/redirect")
  private val clientId = "client-id"
  private val secret = Secret("secret")

  private implicit val backend: SttpBackend[TestEffect, Any] = SttpBackendStub.synchronous

  private val customPathsConfig = AuthorizationCodeProvider.Config(
    loginPath = Path(List(Segment("authorize"))),
    logoutPath = Path(List(Segment("api"), Segment("logout"))),
    tokenPath = Path(List(Segment("api"), Segment("token")))
  )

  List(
    ("Default", AuthorizationCodeProvider.Config.default),
    ("Custom", customPathsConfig)
  ).foreach { case (kind, configuration) => 
    val instance = AuthorizationCodeProvider.uriInstance[TestEffect](baseUri, redirectUri, clientId, secret, configuration)

    s"$kind instance" can {


      "loginLink" should {

        "generate basic login link with default values" in {
          val expected = baseUri
            .withPath(configuration.loginPath.values)
            .addParam("response_type", "code")
            .addParam("client_id", clientId)
            .addParam("redirect_uri", redirectUri.toString())
            .addParam("state", "")
            .addParam("scope", "")
          val result = instance.loginLink()
          result shouldEqual expected
        }

        "generate login link with including state" in {
          val state = "CSRF_TOKEN_CONTENT"
          val expected = baseUri
            .withPath(configuration.loginPath.values)
            .addParam("response_type", "code")
            .addParam("client_id", clientId)
            .addParam("redirect_uri", redirectUri.toString())
            .addParam("state", state)
            .addParam("scope", "")
          val result = instance.loginLink(state = state.some)
          result shouldEqual expected
        }

        "generate login link with including scopes" in {
          val rawScopes = List("users", "domains")
          val scopes = rawScopes.traverse(common.Scope.of).get.toSet
          val expected = baseUri
            .withPath(configuration.loginPath.values)
            .addParam("response_type", "code")
            .addParam("client_id", clientId)
            .addParam("redirect_uri", redirectUri.toString())
            .addParam("state", "")
            .addParam("scope", rawScopes.mkString(" "))
          val result = instance.loginLink(scope = scopes)
          result shouldEqual expected
        }

      }

      "logoutLink" should {

        "generate basic logout link with default values" in {
          val expected = baseUri
            .withPath(configuration.logoutPath.values)
            .addParam("client_id", clientId)
            .addParam("redirect_uri", redirectUri.toString())
          val result = instance.logoutLink()

          result shouldEqual expected
        }

        "generate logout link respecting post logout uri" in {
          val postLogoutUri = Uri.unsafeParse("https://app.example.com/post-logout")
          val expected = baseUri
            .withPath(configuration.logoutPath.values)
            .addParam("client_id", clientId)
            .addParam("redirect_uri", postLogoutUri.toString())
          val result = instance.logoutLink(postLogoutUri.some)

          result shouldEqual expected
        }

      }
    }
  }

}
