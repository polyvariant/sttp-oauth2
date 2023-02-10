package com.ocadotechnology.sttp.oauth2

import io.circe.parser.decode
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationLong

import com.ocadotechnology.sttp.oauth2.codec.CirceEntityDecoders

class TokenSerializationSpec extends AnyWordSpec with Matchers with CirceEntityDecoders {

  private val accessTokenValue = "xxxxxxxxxxxxxxxxxx"
  private val accessToken = Secret(accessTokenValue)
  private val expiresIn: Long = 1800
  private val userName = "john.doe"
  private val domain = "exampledomain"
  private val name = "John Doe"
  private val forename = "John"
  private val surname = "Doe"
  private val mail = "john.doe@example.com"
  private val role1 = "manager"
  private val role2 = "user"
  private val roles = Set(role1, role2)
  private val scope = ""
  private val securityLevel: Long = 384
  private val userId = "c0a8423e-7274-184b"
  private val tokenType = "Bearer"

  "Token" should {

    "deserialize OAuth2Token" in {
      val refreshToken = "yyyyyyyyyyyyyyyyyyyy"

      val jsonToken =
        s"""{
              "access_token": "$accessTokenValue",
              "refresh_token": "$refreshToken",
              "expires_in": $expiresIn,
              "user_name": "$userName",
              "domain": "$domain",
              "user_details": {
                  "username": "$userName",
                  "name": "$name",
                  "forename": "$forename",
                  "surname": "$surname",
                  "mail": "$mail",
                  "cn": "$name",
                  "sn": "$surname"
              },
              "roles": [ "$role1", "$role2" ],
              "scope": "$scope",
              "security_level": $securityLevel,
              "user_id": "$userId",
              "token_type": "$tokenType"
          }"""

      decode[ExtendedOAuth2TokenResponse](jsonToken) shouldBe Right(
        ExtendedOAuth2TokenResponse(
          accessToken,
          refreshToken,
          expiresIn.seconds,
          userName,
          domain,
          TokenUserDetails(userName, name, forename, surname, mail, name, surname),
          roles,
          scope,
          securityLevel,
          userId,
          tokenType
        )
      )
    }

    "deserialize RefreshTokenResponse" in {
      val refreshToken = None

      val jsonToken =
        s"""{
              "access_token": "$accessTokenValue",
              "refresh_token": null,
              "expires_in": $expiresIn,
              "user_name": "$userName",
              "domain": "$domain",
              "user_details": {
                  "username": "$userName",
                  "name": "$name",
                  "forename": "$forename",
                  "surname": "$surname",
                  "mail": "$mail",
                  "cn": "$name",
                  "sn": "$surname"
              },
              "roles": [ "$role1", "$role2" ],
              "scope": "$scope",
              "security_level": $securityLevel,
              "user_id": "$userId",
              "token_type": "$tokenType"
          }"""

      decode[RefreshTokenResponse](jsonToken) shouldBe Right(
        RefreshTokenResponse(
          accessToken,
          refreshToken,
          expiresIn.seconds,
          userName,
          domain,
          TokenUserDetails(userName, name, forename, surname, mail, name, surname),
          roles,
          scope,
          securityLevel,
          userId,
          tokenType
        )
      )
    }
  }

}
