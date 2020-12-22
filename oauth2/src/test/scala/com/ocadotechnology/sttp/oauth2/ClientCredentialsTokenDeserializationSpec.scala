package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidClient
import com.ocadotechnology.sttp.oauth2.common._
import io.circe.DecodingFailure
import io.circe.literal._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse

import scala.concurrent.duration._

class ClientCredentialsTokenDeserializationSpec extends AnyFlatSpec with Matchers with EitherValues {

  "token response JSON" should "be deserialized to proper response" in {
    val json =
      // language=JSON
      json"""{
            "access_token": "TAeJwlzT",
            "domain": "zoo",
            "expires_in": 2399,
            "panda_session_id": "ac097e1f-f927-41df-a776-d824f538351c",
            "scope": "cfc.second-app_scope",
            "token_type": "Bearer"
        }"""

    val response = json.as[Either[OAuth2Error, AccessTokenResponse]]
    response shouldBe Right(
      Right(
        ClientCredentialsToken.AccessTokenResponse(
          accessToken = Secret("TAeJwlzT"),
          domain = "zoo",
          expiresIn = 2399.seconds,
          scope = Scope.refine("cfc.second-app_scope")
        )
      )
    )
  }

  "Token with wrong type" should "not be deserialized" in {
    val json =
      // language=JSON
      json"""{
            "access_token": "TAeJwlzT",
            "domain": "zoo",
            "expires_in": 2399,
            "panda_session_id": "ac097e1f-f927-41df-a776-d824f538351c",
            "scope": "secondapp",
            "token_type": "VeryBadType"
        }"""

    json.as[Either[OAuth2Error, AccessTokenResponse]].left.value shouldBe a[DecodingFailure]
  }

  "JSON with error" should "be deserialized to proper type" in {
    val json =
      // language=JSON
      json"""{
              "error": "invalid_client",
              "error_description": "Client is missing or invalid.",
              "error_uri": "https://pandasso.pages.tech.lastmile.com/documentation/support/panda-errors/token/#invalid_client_client_invalid"
          }"""

    json.as[Either[OAuth2Error, AccessTokenResponse]] shouldBe Right(
      Left(OAuth2ErrorResponse(InvalidClient, "Client is missing or invalid."))
    )
  }

}
