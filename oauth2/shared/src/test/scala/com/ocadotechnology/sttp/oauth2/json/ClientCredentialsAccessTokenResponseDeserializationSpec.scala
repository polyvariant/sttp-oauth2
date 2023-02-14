package com.ocadotechnology.sttp.oauth2.json

import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import com.ocadotechnology.sttp.oauth2.common._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

trait ClientCredentialsAccessTokenResponseDeserializationSpec extends AnyFlatSpec with Matchers with EitherValues {

  this: JsonDecoders =>

  "token response JSON" should "be deserialized to proper case class" in {
    val json =
      // language=JSON
      """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "scope": "secondapp",
            "token_type": "Bearer"
        }"""

    val response = JsonDecoder[ClientCredentialsToken.AccessTokenResponse].decodeString(json)
    response shouldBe Right(
      ClientCredentialsToken.AccessTokenResponse(
        accessToken = Secret("TAeJwlzT"),
        domain = Some("mock"),
        expiresIn = 2399.seconds,
        scope = Scope.of("secondapp")
      )
    )
  }

  "Token with no scope" should "be deserialized" in {
    val json =
      // language=JSON
      """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "token_type": "Bearer"
        }"""

    val response = JsonDecoder[ClientCredentialsToken.AccessTokenResponse].decodeString(json)
    response shouldBe Right(
      ClientCredentialsToken.AccessTokenResponse(
        accessToken = Secret("TAeJwlzT"),
        domain = Some("mock"),
        expiresIn = 2399.seconds,
        scope = None
      )
    )
  }

  "Token with empty scope" should "not be deserialized" in {
    val json =
      // language=JSON
      """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "scope": "",
            "token_type": "Bearer"
        }"""

    JsonDecoder[ClientCredentialsToken.AccessTokenResponse].decodeString(json).left.value shouldBe a[JsonDecoder.Error]
  }

  "Token with wildcard scope" should "not be deserialized" in {
    val json =
      // language=JSON
      """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "scope": " ",
            "token_type": "Bearer"
        }"""

    JsonDecoder[ClientCredentialsToken.AccessTokenResponse].decodeString(json).left.value shouldBe a[JsonDecoder.Error]
  }

  "Token with multiple scope tokens" should "be deserialized" in {
    val json =
      // language=JSON
      """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "scope": "scope1 scope2",
            "token_type": "Bearer"
        }"""

    JsonDecoder[ClientCredentialsToken.AccessTokenResponse].decodeString(json).value shouldBe
      ClientCredentialsToken.AccessTokenResponse(
        accessToken = Secret("TAeJwlzT"),
        domain = Some("mock"),
        expiresIn = 2399.seconds,
        scope = Scope.of("scope1 scope2")
      )

  }

  "Token with wrong type" should "not be deserialized" in {
    val json =
      // language=JSON
      """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "scope": "secondapp",
            "token_type": "BearerToken"
        }"""

    JsonDecoder[ClientCredentialsToken.AccessTokenResponse].decodeString(json).left.value shouldBe a[JsonDecoder.Error]
  }

}
