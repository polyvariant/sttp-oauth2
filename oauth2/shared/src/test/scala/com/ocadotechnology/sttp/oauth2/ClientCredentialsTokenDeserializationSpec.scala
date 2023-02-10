package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidClient
import io.circe.DecodingFailure
import io.circe.parser.decode
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import com.ocadotechnology.sttp.oauth2.codec.CirceEntityDecoders
import io.circe.Decoder
import cats.syntax.all._

class ClientCredentialsTokenDeserializationSpec extends AnyFlatSpec with Matchers with EitherValues with CirceEntityDecoders {

  implicit def bearerTokenResponseDecoder: Decoder[Either[OAuth2Error, AccessTokenResponse]] = {
    def eitherOrFirstError[A, B](aDecoder: Decoder[A], bDecoder: Decoder[B]): Decoder[Either[B, A]] = aDecoder.attempt.flatMap {
      case Right(a) => Decoder.const(a.asRight[B])
      case Left(e) => bDecoder.either(Decoder.failed(e))
    }

    eitherOrFirstError[AccessTokenResponse, OAuth2Error](
      Decoder[AccessTokenResponse],
      Decoder[OAuth2Error]
    )
  }

  "token response JSON" should "be deserialized to proper response" in {
    val json =
      // language=JSON
      """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "panda_session_id": "ac097e1f-f927-41df-a776-d824f538351c",
            "scope": "cfc.second-app_scope",
            "token_type": "Bearer"
        }"""

    val response = decode[Either[OAuth2Error, AccessTokenResponse]](json)
    response shouldBe Right(
      Right(
        ClientCredentialsToken.AccessTokenResponse(
          accessToken = Secret("TAeJwlzT"),
          domain = Some("mock"),
          expiresIn = 2399.seconds,
          scope = Scope.of("cfc.second-app_scope")
        )
      )
    )
  }

  "token response JSON without scope" should "be deserialized to proper response" in {
    val json =
      // language=JSON
      """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "panda_session_id": "ac097e1f-f927-41df-a776-d824f538351c",
            "token_type": "Bearer"
        }"""

    val response = decode[Either[OAuth2Error, AccessTokenResponse]](json)
    response shouldBe Right(
      Right(
        ClientCredentialsToken.AccessTokenResponse(
          accessToken = Secret("TAeJwlzT"),
          domain = Some("mock"),
          expiresIn = 2399.seconds,
          scope = None
        )
      )
    )
  }

  "Token with wrong type" should "not be deserialized" in {
    val json =
      // language=JSON
      """{
            "access_token": "TAeJwlzT",
            "domain": "mock",
            "expires_in": 2399,
            "panda_session_id": "ac097e1f-f927-41df-a776-d824f538351c",
            "scope": "secondapp",
            "token_type": "VeryBadType"
        }"""

    decode[Either[OAuth2Error, AccessTokenResponse]](json).left.value shouldBe a[DecodingFailure]
  }

  "JSON with error" should "be deserialized to proper type" in {
    val json =
      // language=JSON
      """{
              "error": "invalid_client",
              "error_description": "Client is missing or invalid.",
              "error_uri": "https://pandasso.pages.tech.lastmile.com/documentation/support/panda-errors/token/#invalid_client_client_invalid"
          }"""

    decode[Either[OAuth2Error, AccessTokenResponse]](json) shouldBe Right(
      Left(OAuth2ErrorResponse(InvalidClient, Some("Client is missing or invalid.")))
    )
  }

  "JSON with error without optional fields" should "be deserialized to proper type" in {
    val json =
      // language=JSON
      """{
              "error": "invalid_client"
          }"""

    decode[Either[OAuth2Error, AccessTokenResponse]](json) shouldBe Right(
      Left(OAuth2ErrorResponse(InvalidClient, None))
    )
  }

}
