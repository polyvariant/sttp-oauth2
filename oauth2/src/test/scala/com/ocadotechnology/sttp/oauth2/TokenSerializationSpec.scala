package com.ocadotechnology.sttp.oauth2

import org.scalatest.wordspec.AnyWordSpec
import io.circe.literal._
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationLong

class TokenSerializationSpec extends AnyWordSpec with Matchers {

  val accessTokenValue = "xxxxxxxxxxxxxxxxxx"
  val accessToken = Secret(accessTokenValue)
  val expiresIn: Long = 1800
  val userName = "john.doe"
  val domain = "exampledomain"
  val name = "John Doe"
  val forename = "John"
  val surname = "Doe"
  val mail = "john.doe@example.com"
  val roles = Set[String]("manager")
  val scope = ""
  val securityLevel: Long = 384
  val userId = "c0a8423e-7274-184b"
  val tokenType = "Bearer"

  "Token" should {

    "deserialize OAuth2Token" in {
      val refreshToken = "yyyyyyyyyyyyyyyyyyyy"

      val jsonToken =
        json"""{
              "access_token": $accessTokenValue,
              "refresh_token": $refreshToken,
              "expires_in": $expiresIn,
              "user_name": $userName,
              "domain": $domain,
              "user_details": {
                  "username": $userName,
                  "name": $name,
                  "forename": $forename,
                  "surname": $surname,
                  "mail": $mail,
                  "cn": $name,
                  "sn": $surname
              },
              "roles": $roles,
              "scope": $scope,
              "security_level": $securityLevel,
              "user_id": $userId,
              "token_type": $tokenType
          }"""

      jsonToken.as[ExtendedOAuth2TokenResponse] shouldBe Right(
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
        json"""{
              "access_token": $accessTokenValue,
              "refresh_token": $refreshToken,
              "expires_in": $expiresIn,
              "user_name": $userName,
              "domain": $domain,
              "user_details": {
                  "username": $userName,
                  "name": $name,
                  "forename": $forename,
                  "surname": $surname,
                  "mail": $mail,
                  "cn": $name,
                  "sn": $surname
              },
              "roles": $roles,
              "scope": $scope,
              "security_level": $securityLevel,
              "user_id": $userId,
              "token_type": $tokenType
          }"""

      jsonToken.as[RefreshTokenResponse] shouldBe Right(
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
