package org.polyvariant.sttp.oauth2

import cats.syntax.all._
import org.polyvariant.sttp.oauth2.common._
import org.polyvariant.sttp.oauth2.common.Error
import org.polyvariant.sttp.oauth2.common.Error.OAuth2ErrorResponse
import org.polyvariant.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidClient
import org.polyvariant.sttp.oauth2.json.JsonDecoders
import org.polyvariant.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import org.polyvariant.sttp.oauth2.common.Error.OAuth2Error
import org.polyvariant.sttp.oauth2.json.JsonDecoder
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

trait ClientCredentialsTokenDeserializationSpec extends AnyFlatSpec with Matchers with EitherValues {

  this: JsonDecoders =>

  implicit def bearerTokenResponseDecoder: JsonDecoder[Either[Error.OAuth2Error, AccessTokenResponse]] = {
    def eitherOrFirstError[A, B](aDecoder: JsonDecoder[A], bDecoder: JsonDecoder[B]): JsonDecoder[Either[B, A]] =
      new JsonDecoder[Either[B, A]] {

        override def decodeString(data: String): Either[JsonDecoder.Error, Either[B, A]] =
          aDecoder.decodeString(data) match {
            case Right(a)         => a.asRight[B].asRight[JsonDecoder.Error]
            case Left(firstError) =>
              bDecoder.decodeString(data).fold(_ => firstError.asLeft[Either[B, A]], _.asLeft[A].asRight[JsonDecoder.Error])
          }

      }

    eitherOrFirstError[AccessTokenResponse, OAuth2Error](
      JsonDecoder[AccessTokenResponse],
      JsonDecoder[OAuth2Error]
    )
  }

  "token response JSON" should "be deserialized to proper response" in {
    val json =
      // language=JSON
      """
      {
        "access_token": "TAeJwlzT",
        "domain": "mock",
        "expires_in": 2399,
        "panda_session_id": "ac097e1f-f927-41df-a776-d824f538351c",
        "scope": "cfc.second-app_scope",
        "token_type": "Bearer"
      }
      """

    val response = JsonDecoder[Either[OAuth2Error, AccessTokenResponse]].decodeString(json)
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
      """
      {
        "access_token": "TAeJwlzT",
        "domain": "mock",
        "expires_in": 2399,
        "panda_session_id": "ac097e1f-f927-41df-a776-d824f538351c",
        "token_type": "Bearer"
      }
      """

    val response = JsonDecoder[Either[OAuth2Error, AccessTokenResponse]].decodeString(json)
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

  "token response JSON with empty scope" should "be deserialized to proper response with None scope" in {
    val json =
      // language=JSON
      """
      {
        "access_token": "TAeJwlzT",
        "domain": "mock",
        "expires_in": 2399,
        "scope": "",
        "panda_session_id": "ac097e1f-f927-41df-a776-d824f538351c",
        "token_type": "Bearer"
      }
      """

    val response = JsonDecoder[Either[OAuth2Error, AccessTokenResponse]].decodeString(json)
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
      """
      {
        "error": "invalid_client",
        "error_description": "Client is missing or invalid.",
        "error_uri": "https://pandasso.pages.tech.lastmile.com/documentation/support/panda-errors/token/#invalid_client_client_invalid"
      }
      """

    JsonDecoder[Either[OAuth2Error, AccessTokenResponse]].decodeString(json) shouldBe Right(
      Left(OAuth2ErrorResponse(InvalidClient, Some("Client is missing or invalid.")))
    )
  }

  "JSON with error without optional fields" should "be deserialized to proper type" in {
    val json =
      // language=JSON
      """
      {
        "error": "invalid_client"
      }
      """

    JsonDecoder[Either[OAuth2Error, AccessTokenResponse]].decodeString(json) shouldBe Right(
      Left(OAuth2ErrorResponse(InvalidClient, None))
    )
  }

}
