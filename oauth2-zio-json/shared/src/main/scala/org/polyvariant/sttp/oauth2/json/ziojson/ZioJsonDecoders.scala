package org.polyvariant.sttp.oauth2.json.ziojson

import org.polyvariant.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import org.polyvariant.sttp.oauth2.ExtendedOAuth2TokenResponse
import org.polyvariant.sttp.oauth2.Introspection.Audience
import org.polyvariant.sttp.oauth2.Introspection.SeqAudience
import org.polyvariant.sttp.oauth2.Introspection.StringAudience
import org.polyvariant.sttp.oauth2.Introspection.TokenIntrospectionResponse
import org.polyvariant.sttp.oauth2.OAuth2TokenResponse
import org.polyvariant.sttp.oauth2.RefreshTokenResponse
import org.polyvariant.sttp.oauth2.Secret
import org.polyvariant.sttp.oauth2.TokenUserDetails
import org.polyvariant.sttp.oauth2.UserInfo
import org.polyvariant.sttp.oauth2.common.Error.OAuth2Error
import org.polyvariant.sttp.oauth2.common.Scope
import org.polyvariant.sttp.oauth2.json.{JsonDecoder => OAuth2JsonDecoder}
import zio.json._
import zio.json.jsonMemberNames
import zio.json.SnakeCase

import java.time.Instant
import scala.concurrent.duration.DurationLong
import scala.concurrent.duration.FiniteDuration

trait ZioJsonDecoders {
  import ZioJsonDecoders._

  implicit def jsonDecoder[A](
    implicit decoder: JsonDecoder[A]
  ): OAuth2JsonDecoder[A] =
    (data: String) => decoder.decodeJson(data).left.map(msg => OAuth2JsonDecoder.Error(msg))

  implicit val secretStringDecoder: JsonDecoder[Secret[String]] =
    JsonDecoder.string.map(Secret(_))

  implicit val secondsDecoder: JsonDecoder[FiniteDuration] =
    JsonDecoder.long.map(_.seconds)

  implicit val instantDecoder: JsonDecoder[Instant] =
    JsonDecoder.long.map(Instant.ofEpochSecond)

  implicit val scopeDecoder: JsonDecoder[Scope] =
    JsonDecoder.string.mapOrFail { value =>
      Scope.from(value).left.map(identity)
    }

  implicit val optionScopeDecoder: JsonDecoder[Option[Scope]] =
    JsonDecoder.option[String].mapOrFail {
      case None | Some("") => Right(None)
      case Some(value)     => Scope.from(value).map(Some(_)).left.map(identity)
    }

  implicit val tokenUserDetailsDecoder: JsonDecoder[TokenUserDetails] =
    DeriveJsonDecoder.gen[TokenUserDetails]

  implicit val userInfoDecoder: JsonDecoder[UserInfo] =
    userInfoRawDecoder.map { raw =>
      UserInfo(
        raw.sub,
        raw.name,
        raw.givenName,
        raw.familyName,
        raw.jobTitle,
        raw.domain,
        raw.preferredUsername,
        raw.email,
        raw.emailVerified,
        raw.locale,
        raw.sites.getOrElse(Nil),
        raw.banners.getOrElse(Nil),
        raw.regions.getOrElse(Nil),
        raw.fulfillmentContexts.getOrElse(Nil)
      )
    }

  implicit val accessTokenResponseDecoder: JsonDecoder[AccessTokenResponse] =
    accessTokenResponseRawDecoder.mapOrFail { raw =>
      if (raw.tokenType.equalsIgnoreCase("Bearer"))
        Right(AccessTokenResponse(raw.accessToken, raw.domain, raw.expiresIn, raw.scope))
      else
        Left(s"Error while decoding '.token_type': value '${raw.tokenType}' is not equal to 'Bearer'")
    }

  implicit val oAuth2ErrorDecoder: JsonDecoder[OAuth2Error] =
    oAuth2ErrorRawDecoder.map { raw =>
      OAuth2Error.fromErrorTypeAndDescription(raw.error, raw.errorDescription)
    }

  implicit val audienceDecoder: JsonDecoder[Audience] =
    JsonDecoder
      .string
      .map(StringAudience(_))
      .orElse(
        JsonDecoder.list[String].map(seq => SeqAudience(seq))
      )

  implicit val oAuth2TokenResponseDecoder: JsonDecoder[OAuth2TokenResponse] = {
    implicit val secretDec: JsonDecoder[Secret[String]] = secretStringDecoder
    implicit val secondsDec: JsonDecoder[FiniteDuration] = secondsDecoder
    DeriveJsonDecoder.gen[OAuth2TokenResponse]
  }

  implicit val extendedOAuth2TokenResponseDecoder: JsonDecoder[ExtendedOAuth2TokenResponse] = {
    implicit val secretDec: JsonDecoder[Secret[String]] = secretStringDecoder
    implicit val secondsDec: JsonDecoder[FiniteDuration] = secondsDecoder
    implicit val tokenUserDec: JsonDecoder[TokenUserDetails] = tokenUserDetailsDecoder
    DeriveJsonDecoder.gen[ExtendedOAuth2TokenResponse]
  }

  implicit val tokenIntrospectionResponseDecoder: JsonDecoder[TokenIntrospectionResponse] = {
    implicit val instantDec: JsonDecoder[Instant] = instantDecoder
    implicit val optionScopeDec: JsonDecoder[Option[Scope]] = optionScopeDecoder
    implicit val audienceDec: JsonDecoder[Audience] = audienceDecoder
    DeriveJsonDecoder.gen[TokenIntrospectionResponse]
  }

  implicit val refreshTokenResponseDecoder: JsonDecoder[RefreshTokenResponse] = {
    implicit val secretDec: JsonDecoder[Secret[String]] = secretStringDecoder
    implicit val secondsDec: JsonDecoder[FiniteDuration] = secondsDecoder
    implicit val tokenUserDec: JsonDecoder[TokenUserDetails] = tokenUserDetailsDecoder
    DeriveJsonDecoder.gen[RefreshTokenResponse]
  }

}

object ZioJsonDecoders {

  // Base decoders needed for derivation
  private[ziojson] implicit val secretStringDecoder: JsonDecoder[Secret[String]] =
    JsonDecoder.string.map(Secret(_))

  private[ziojson] implicit val secondsDecoder: JsonDecoder[FiniteDuration] =
    JsonDecoder.long.map(_.seconds)

  private[ziojson] implicit val instantDecoder: JsonDecoder[Instant] =
    JsonDecoder.long.map(Instant.ofEpochSecond)

  private[ziojson] implicit val scopeDecoder: JsonDecoder[Scope] =
    JsonDecoder.string.mapOrFail { value =>
      Scope.from(value).left.map(identity)
    }

  private[ziojson] implicit val optionScopeDecoder: JsonDecoder[Option[Scope]] =
    JsonDecoder.option[String].mapOrFail {
      case None | Some("") => Right(None)
      case Some(value)     => Scope.from(value).map(Some(_)).left.map(identity)
    }

  private[ziojson] implicit val tokenUserDetailsDecoder: JsonDecoder[TokenUserDetails] =
    DeriveJsonDecoder.gen[TokenUserDetails]

  private[ziojson] implicit val audienceDecoder: JsonDecoder[Audience] =
    JsonDecoder
      .string
      .map(StringAudience(_))
      .orElse(
        JsonDecoder.list[String].map(seq => SeqAudience(seq))
      )

  // Using Raw copy because the original UserInfo has list fields with default values (Nil)
  // that need special handling to map Option[List[String]] -> List[String]
  @jsonMemberNames(SnakeCase)
  private final case class UserInfoRaw(
    sub: Option[String],
    name: Option[String],
    givenName: Option[String],
    familyName: Option[String],
    jobTitle: Option[String],
    domain: Option[String],
    preferredUsername: Option[String],
    email: Option[String],
    emailVerified: Option[Boolean],
    locale: Option[String],
    sites: Option[List[String]],
    banners: Option[List[String]],
    regions: Option[List[String]],
    fulfillmentContexts: Option[List[String]]
  )

  private val userInfoRawDecoder: JsonDecoder[UserInfoRaw] =
    DeriveJsonDecoder.gen[UserInfoRaw]

  // Using Raw copy because we need to validate tokenType = "Bearer"
  @jsonMemberNames(SnakeCase)
  private final case class AccessTokenResponseRaw(
    accessToken: Secret[String],
    domain: Option[String],
    expiresIn: FiniteDuration,
    scope: Option[Scope],
    tokenType: String
  )

  private val accessTokenResponseRawDecoder: JsonDecoder[AccessTokenResponseRaw] =
    DeriveJsonDecoder.gen[AccessTokenResponseRaw]

  // Using Raw copy because we need custom construction via fromErrorTypeAndDescription
  @jsonMemberNames(SnakeCase)
  private final case class OAuth2ErrorRaw(
    error: String,
    errorDescription: Option[String]
  )

  private val oAuth2ErrorRawDecoder: JsonDecoder[OAuth2ErrorRaw] =
    DeriveJsonDecoder.gen[OAuth2ErrorRaw]

}
