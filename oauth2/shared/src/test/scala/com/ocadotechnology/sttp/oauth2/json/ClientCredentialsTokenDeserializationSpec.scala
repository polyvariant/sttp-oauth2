package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidClient
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import com.ocadotechnology.sttp.oauth2.json.JsonDecoders
import com.ocadotechnology.sttp.oauth2.common.Error
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.codec.EntityDecoder
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import cats.syntax.all._

trait ClientCredentialsTokenDeserializationSpec extends AnyFlatSpec with Matchers with EitherValues {

  this: JsonDecoders =>

  implicit def bearerTokenResponseDecoder: EntityDecoder[Either[Error.OAuth2Error, AccessTokenResponse]] = {
    def eitherOrFirstError[A, B](aDecoder: EntityDecoder[A], bDecoder: EntityDecoder[B]): EntityDecoder[Either[B, A]] =
      new EntityDecoder[Either[B, A]] {

        override def decodeString(data: String): Either[EntityDecoder.Error, Either[B, A]] =
          aDecoder.decodeString(data) match {
            case Right(a) => a.asRight[B].asRight[EntityDecoder.Error]
            case Left(firstError) =>
              bDecoder.decodeString(data).fold(_ => firstError.asLeft[Either[B, A]], _.asLeft[A].asRight[EntityDecoder.Error])
          }

      }

    eitherOrFirstError[AccessTokenResponse, OAuth2Error](
      EntityDecoder[AccessTokenResponse],
      EntityDecoder[OAuth2Error]
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

    val response = EntityDecoder[Either[OAuth2Error, AccessTokenResponse]].decodeString(json)
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

    val response = EntityDecoder[Either[OAuth2Error, AccessTokenResponse]].decodeString(json)
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

  "JSON with error" should "be deserialized to proper type" in {
    val json =
      // language=JSON
      """{
              "error": "invalid_client",
              "error_description": "Client is missing or invalid.",
              "error_uri": "https://pandasso.pages.tech.lastmile.com/documentation/support/panda-errors/token/#invalid_client_client_invalid"
          }"""

    EntityDecoder[Either[OAuth2Error, AccessTokenResponse]].decodeString(json) shouldBe Right(
      Left(OAuth2ErrorResponse(InvalidClient, Some("Client is missing or invalid.")))
    )
  }

  "JSON with error without optional fields" should "be deserialized to proper type" in {
    val json =
      // language=JSON
      """{
              "error": "invalid_client"
          }"""

    EntityDecoder[Either[OAuth2Error, AccessTokenResponse]].decodeString(json) shouldBe Right(
      Left(OAuth2ErrorResponse(InvalidClient, None))
    )
  }

}
