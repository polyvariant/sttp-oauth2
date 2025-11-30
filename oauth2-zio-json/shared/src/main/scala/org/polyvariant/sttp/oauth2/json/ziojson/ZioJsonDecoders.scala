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

  implicit val oAuth2TokenResponseDecoder: JsonDecoder[OAuth2TokenResponse] =
    oAuth2TokenResponseRawDecoder.map { raw =>
      OAuth2TokenResponse(raw.accessToken, raw.scope, raw.tokenType, raw.expiresIn, raw.refreshToken)
    }

  implicit val extendedOAuth2TokenResponseDecoder: JsonDecoder[ExtendedOAuth2TokenResponse] =
    extendedOAuth2TokenResponseRawDecoder.map { raw =>
      ExtendedOAuth2TokenResponse(
        raw.accessToken,
        raw.refreshToken,
        raw.expiresIn,
        raw.userName,
        raw.domain,
        raw.userDetails,
        raw.roles,
        raw.scope,
        raw.securityLevel,
        raw.userId,
        raw.tokenType
      )
    }

  implicit val audienceDecoder: JsonDecoder[Audience] =
    JsonDecoder
      .string
      .map(StringAudience(_))
      .orElse(
        JsonDecoder.list[String].map(seq => SeqAudience(seq))
      )

  implicit val tokenIntrospectionResponseDecoder: JsonDecoder[TokenIntrospectionResponse] =
    tokenIntrospectionResponseRawDecoder.map { raw =>
      TokenIntrospectionResponse(
        raw.active,
        raw.clientId,
        raw.domain,
        raw.exp,
        raw.iat,
        raw.nbf,
        raw.authorities,
        raw.scope,
        raw.tokenType,
        raw.sub,
        raw.iss,
        raw.jti,
        raw.aud
      )
    }

  implicit val refreshTokenResponseDecoder: JsonDecoder[RefreshTokenResponse] =
    refreshTokenResponseRawDecoder.map { raw =>
      RefreshTokenResponse(
        raw.accessToken,
        raw.refreshToken,
        raw.expiresIn,
        raw.userName,
        raw.domain,
        raw.userDetails,
        raw.roles,
        raw.scope,
        raw.securityLevel,
        raw.userId,
        raw.tokenType
      )
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

  @jsonMemberNames(SnakeCase)
  private[ziojson] final case class UserInfoRaw(
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

  private[ziojson] val userInfoRawDecoder: JsonDecoder[UserInfoRaw] =
    DeriveJsonDecoder.gen[UserInfoRaw]

  @jsonMemberNames(SnakeCase)
  private[ziojson] final case class AccessTokenResponseRaw(
    accessToken: Secret[String],
    domain: Option[String],
    expiresIn: FiniteDuration,
    scope: Option[Scope],
    tokenType: String
  )

  private[ziojson] val accessTokenResponseRawDecoder: JsonDecoder[AccessTokenResponseRaw] =
    DeriveJsonDecoder.gen[AccessTokenResponseRaw]

  @jsonMemberNames(SnakeCase)
  private[ziojson] final case class OAuth2ErrorRaw(
    error: String,
    errorDescription: Option[String]
  )

  private[ziojson] val oAuth2ErrorRawDecoder: JsonDecoder[OAuth2ErrorRaw] =
    DeriveJsonDecoder.gen[OAuth2ErrorRaw]

  @jsonMemberNames(SnakeCase)
  private[ziojson] final case class OAuth2TokenResponseRaw(
    accessToken: Secret[String],
    scope: String,
    tokenType: String,
    expiresIn: Option[FiniteDuration],
    refreshToken: Option[String]
  )

  private[ziojson] val oAuth2TokenResponseRawDecoder: JsonDecoder[OAuth2TokenResponseRaw] =
    DeriveJsonDecoder.gen[OAuth2TokenResponseRaw]

  @jsonMemberNames(SnakeCase)
  private[ziojson] final case class ExtendedOAuth2TokenResponseRaw(
    accessToken: Secret[String],
    refreshToken: String,
    expiresIn: FiniteDuration,
    userName: String,
    domain: String,
    userDetails: TokenUserDetails,
    roles: Set[String],
    scope: String,
    securityLevel: Long,
    userId: String,
    tokenType: String
  )

  private[ziojson] val extendedOAuth2TokenResponseRawDecoder: JsonDecoder[ExtendedOAuth2TokenResponseRaw] =
    DeriveJsonDecoder.gen[ExtendedOAuth2TokenResponseRaw]

  @jsonMemberNames(SnakeCase)
  private[ziojson] final case class TokenIntrospectionResponseRaw(
    active: Boolean,
    clientId: Option[String],
    domain: Option[String],
    exp: Option[Instant],
    iat: Option[Instant],
    nbf: Option[Instant],
    authorities: Option[List[String]],
    scope: Option[Scope],
    tokenType: Option[String],
    sub: Option[String],
    iss: Option[String],
    jti: Option[String],
    aud: Option[Audience]
  )

  private[ziojson] val tokenIntrospectionResponseRawDecoder: JsonDecoder[TokenIntrospectionResponseRaw] =
    DeriveJsonDecoder.gen[TokenIntrospectionResponseRaw]

  @jsonMemberNames(SnakeCase)
  private[ziojson] final case class RefreshTokenResponseRaw(
    accessToken: Secret[String],
    refreshToken: Option[String],
    expiresIn: FiniteDuration,
    userName: String,
    domain: String,
    userDetails: TokenUserDetails,
    roles: Set[String],
    scope: String,
    securityLevel: Long,
    userId: String,
    tokenType: String
  )

  private[ziojson] val refreshTokenResponseRawDecoder: JsonDecoder[RefreshTokenResponseRaw] =
    DeriveJsonDecoder.gen[RefreshTokenResponseRaw]

}
