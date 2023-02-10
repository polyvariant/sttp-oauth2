package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

import com.ocadotechnology.sttp.oauth2.codec.CirceEntityDecoders

class ClientCredentialsAccessTokenResponseDeserializationSpec extends AnyFlatSpec with Matchers with EitherValues with CirceEntityDecoders {

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

    val response = decode[ClientCredentialsToken.AccessTokenResponse](json)
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

    val response = decode[ClientCredentialsToken.AccessTokenResponse](json)
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

    decode[ClientCredentialsToken.AccessTokenResponse](json).left.value shouldBe a[DecodingFailure]
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

    decode[ClientCredentialsToken.AccessTokenResponse](json).left.value shouldBe a[DecodingFailure]
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

    decode[ClientCredentialsToken.AccessTokenResponse](json).value shouldBe
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

    decode[ClientCredentialsToken.AccessTokenResponse](json).left.value shouldBe a[DecodingFailure]
  }

}
