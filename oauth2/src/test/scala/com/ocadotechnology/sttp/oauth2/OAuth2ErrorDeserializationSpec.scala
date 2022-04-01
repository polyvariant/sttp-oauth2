package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidClient
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidGrant
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidRequest
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidScope
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.UnauthorizedClient
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.UnsupportedGrantType
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse
import com.ocadotechnology.sttp.oauth2.common.Error.UnknownOAuth2Error
import io.circe.DecodingFailure
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser.decode

class OAuth2ErrorDeserializationSpec extends AnyFlatSpec with Matchers with EitherValues {

  private def check[A <: OAuth2Error](json: String, deserialized: A) =
    decode[OAuth2Error](json) shouldBe Right(deserialized)

  "invalid_request error JSON" should "be deserialized to InvalidRequest" in {
    check(
      // language=JSON
      """{
            "error": "invalid_request",
            "error_description": "Grant type is missing.",
            "error_uri": "https://example.com/errors/invalid_request"
        }""",
      OAuth2ErrorResponse(InvalidRequest, Some("Grant type is missing."))
    )
  }

  "invalid_client error JSON" should "be deserialized to InvalidClient" in {
    check(
      // language=JSON
      """{
            "error": "invalid_client",
            "error_description": "Client is missing or invalid.",
            "error_uri": "https://example.com/errors/invalid_client"
        }""",
      OAuth2ErrorResponse(InvalidClient, Some("Client is missing or invalid."))
    )
  }

  "invalid_grant error JSON" should "be deserialized to InvalidGrant" in {
    check(
      // language=JSON
      """{
            "error": "invalid_grant",
            "error_description": "Provided domain cannot be used with given grant type.",
            "error_uri": "https://example.com/errors/invalid_grant"
        }""",
      OAuth2ErrorResponse(InvalidGrant, Some("Provided domain cannot be used with given grant type."))
    )
  }

  "unauthorized_client error JSON" should "be deserialized to UnauthorizedClient" in {
    check(
      // language=JSON
      """{
            "error": "unauthorized_client",
            "error_description": "Client is not allowed to use provided grant type.",
            "error_uri": "https://example.com/errors/unauthorized_client"
        }""",
      OAuth2ErrorResponse(UnauthorizedClient, Some("Client is not allowed to use provided grant type."))
    )
  }

  "unsupported_grant_type error JSON" should "be deserialized to InvalidGrant" in {
    check(
      // language=JSON
      """{
            "error": "unsupported_grant_type",
            "error_description": "Requested grant type is invalid.",
            "error_uri": "https://example.com/errors/unsupported_grant_type"
        }""",
      OAuth2ErrorResponse(UnsupportedGrantType, Some("Requested grant type is invalid."))
    )
  }

  "invalid_scope error JSON" should "be deserialized to InvalidGrant" in {
    check(
      // language=JSON
      """{
            "error": "invalid_scope",
            "error_description": "Client is not allowed to use requested scope.",
            "error_uri": "https://example.com/errors/invalid_scope"
        }""",
      OAuth2ErrorResponse(InvalidScope, Some("Client is not allowed to use requested scope."))
    )
  }

  "invalid_token error JSON" should "be deserialized to Unknown" in {
    check(
      // language=JSON
      """{
            "error": "invalid_token",
            "error_description": "Invalid access token.",
            "error_uri": "https://example.com/errors/invalid_token"
        }""",
      UnknownOAuth2Error(error = "invalid_token", Some("Invalid access token."))
    )
  }

  "insufficient_scope error JSON" should "be deserialized to Unknown" in {
    check(
      // language=JSON
      """{
            "error": "insufficient_scope",
            "error_description": "Access token does not contain requested scope.",
            "error_uri": "https://example.com/errors/insufficient_scope"
        }""",
      UnknownOAuth2Error(error = "insufficient_scope", Some("Access token does not contain requested scope."))
    )
  }

  "unknown error JSON" should "be deserialized to Unknown" in {
    check(
      // language=JSON
      """{
            "error": "unknown_error",
            "error_description": "I don't know this error type.",
            "error_uri": "https://example.com/errors/unknown_error"
        }""",
      UnknownOAuth2Error(error = "unknown_error", errorDescription = Some("I don't know this error type."))
    )
  }

  "JSON in wrong format" should "not be deserialized" in {
    val json =
      // language=JSON
      """{
            "error_type": "some_error",
            "description": "YOLO"
        }"""

    decode[OAuth2Error](json).left.value shouldBe a[DecodingFailure]
  }

}
