package com.ocadotechnology.sttp.oauth2.json.circe

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.UserInfo
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.ExtendedOAuth2TokenResponse
import com.ocadotechnology.sttp.oauth2.Introspection.Audience
import com.ocadotechnology.sttp.oauth2.Introspection.SeqAudience
import com.ocadotechnology.sttp.oauth2.Introspection.StringAudience
import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.OAuth2TokenResponse
import com.ocadotechnology.sttp.oauth2.RefreshTokenResponse
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.TokenUserDetails
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import io.circe.Decoder
import io.circe.refined._

import java.time.Instant
import scala.concurrent.duration.DurationLong
import scala.concurrent.duration.FiniteDuration

trait CirceJsonDecoders {

  implicit def jsonDecoder[A](implicit decoder: Decoder[A]): JsonDecoder[A] =
    (data: String) => io.circe.parser.decode[A](data).leftMap(error => JsonDecoder.Error(error.getMessage, cause = Some(error)))

  implicit val userInfoDecoder: Decoder[UserInfo] = (
    Decoder[Option[String]].at("sub"),
    Decoder[Option[String]].at("name"),
    Decoder[Option[String]].at("given_name"),
    Decoder[Option[String]].at("family_name"),
    Decoder[Option[String]].at("job_title"),
    Decoder[Option[String]].at("domain"),
    Decoder[Option[String]].at("preferred_username"),
    Decoder[Option[String]].at("email"),
    Decoder[Option[Boolean]].at("email_verified"),
    Decoder[Option[String]].at("locale"),
    Decoder[List[String]].at("sites").or(Decoder.const(List.empty[String])),
    Decoder[List[String]].at("banners").or(Decoder.const(List.empty[String])),
    Decoder[List[String]].at("regions").or(Decoder.const(List.empty[String])),
    Decoder[List[String]].at("fulfillment_contexts").or(Decoder.const(List.empty[String]))
  ).mapN(UserInfo.apply)

  implicit val secondsDecoder: Decoder[FiniteDuration] = Decoder.decodeLong.map(_.seconds)

  implicit val instantDecoder: Decoder[Instant] = Decoder.decodeLong.map(Instant.ofEpochSecond)

  implicit val tokenDecoder: Decoder[AccessTokenResponse] =
    Decoder
      .forProduct4(
        "access_token",
        "domain",
        "expires_in",
        "scope"
      )(AccessTokenResponse.apply)
      .validate {
        _.downField("token_type").as[String] match {
          case Right(value) if value.equalsIgnoreCase("Bearer") => List.empty
          case Right(string) => List(s"Error while decoding '.token_type': value '$string' is not equal to 'Bearer'")
          case Left(s)       => List(s"Error while decoding '.token_type': ${s.getMessage}")
        }
      }

  implicit val errorDecoder: Decoder[OAuth2Error] =
    Decoder.forProduct2[OAuth2Error, String, Option[String]]("error", "error_description")(OAuth2Error.fromErrorTypeAndDescription)

  implicit val tokenResponseDecoder: Decoder[OAuth2TokenResponse] =
    Decoder.forProduct5(
      "access_token",
      "scope",
      "token_type",
      "expires_in",
      "refresh_token"
    )(OAuth2TokenResponse.apply)

  implicit val tokenUserDetailsDecoder: Decoder[TokenUserDetails] =
    Decoder.forProduct7(
      "username",
      "name",
      "forename",
      "surname",
      "mail",
      "cn",
      "sn"
    )(TokenUserDetails.apply)

  implicit val extendedTokenResponseDecoder: Decoder[ExtendedOAuth2TokenResponse] =
    Decoder.forProduct11(
      "access_token",
      "refresh_token",
      "expires_in",
      "user_name",
      "domain",
      "user_details",
      "roles",
      "scope",
      "security_level",
      "user_id",
      "token_type"
    )(ExtendedOAuth2TokenResponse.apply)

  implicit val audienceDecoder: Decoder[Audience] =
    Decoder.decodeString.map(StringAudience.apply).or(Decoder.decodeSeq[String].map(SeqAudience.apply))

  implicit val tokenIntrospectionResponseDecoder: Decoder[TokenIntrospectionResponse] =
    Decoder.forProduct13(
      "active",
      "client_id",
      "domain",
      "exp",
      "iat",
      "nbf",
      "authorities",
      "scope",
      "token_type",
      "sub",
      "iss",
      "jti",
      "aud"
    )(TokenIntrospectionResponse.apply)

  implicit val refreshTokenResponseDecoder: Decoder[RefreshTokenResponse] =
    Decoder.forProduct11(
      "access_token",
      "refresh_token",
      "expires_in",
      "user_name",
      "domain",
      "user_details",
      "roles",
      "scope",
      "security_level",
      "user_id",
      "token_type"
    )(RefreshTokenResponse.apply)

  implicit def secretDecoder[A: Decoder]: Decoder[Secret[A]] = Decoder[A].map(Secret(_))

}
