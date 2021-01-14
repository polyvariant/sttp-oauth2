package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder
import io.circe.HCursor

/** Models user info as defined in open id standard
  * @see https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
  *
  * @param sub Subject - Identifier for the End-User at the Issuer
  * @param name End-User's full name
  * @param givenName Given name(s) or first name(s) of the End-User
  * @param familyName Surname(s) or last name(s) of the End-User
  * @param jobTitle Job Title of the End-User.
  * @param domain Domain of the End-User
  * @param preferredUsername Username of the End-User
  * @param email End-User's preferred e-mail address
  * @param emailVerified True if the End-User's e-mail address has been verified; otherwise false
  * @param locale Locale of the End-User
  * @param sites List of sites of the End-User.
  * @param banners List of banners End-User has access to.
  * @param regions List of regions End-User has access to.
  * @param fulfillmentContexts List of fulfillment contexts End-User has access to.
  */
final case class UserInfo(
  sub: Option[String],
  name: Option[String],
  givenName: Option[String],
  familyName: Option[String], // TODO - non-standard field
  jobTitle: Option[String],
  domain: Option[String],
  preferredUsername: Option[String],
  email: Option[String],
  emailVerified: Option[Boolean],
  locale: Option[String],
  // TODO - fields below are non-standard and should be removed when introducing custom UserInfoType with decoder
  sites: List[String] = Nil,
  banners: List[String] = Nil,
  regions: List[String] = Nil,
  fulfillmentContexts: List[String] = Nil
)

object UserInfo {

  implicit val decoder: Decoder[UserInfo] = new Decoder[UserInfo] {

    final def apply(c: HCursor): Decoder.Result[UserInfo] = for {
      sub                 <- c.downField("sub").as[Option[String]]
      name                <- c.downField("name").as[Option[String]]
      givenName           <- c.downField("given_name").as[Option[String]]
      familyName          <- c.downField("family_name").as[Option[String]]
      jobTitle            <- c.downField("job_title").as[Option[String]]
      domain              <- c.downField("domain").as[Option[String]]
      preferredUsername   <- c.downField("preferred_username").as[Option[String]]
      email               <- c.downField("email").as[Option[String]]
      emailVerified       <- c.downField("email_verified").as[Option[Boolean]]
      locale              <- c.downField("locale").as[Option[String]]
      sites               <- c.downField("sites").as[List[String]].orElse(Right(Nil))
      banners             <- c.downField("banners").as[List[String]].orElse(Right(Nil))
      regions             <- c.downField("regions").as[List[String]].orElse(Right(Nil))
      fulfillmentContexts <- c.downField("fulfillment_contexts").as[List[String]].orElse(Right(Nil))
    } yield new UserInfo(
      sub,
      name,
      givenName,
      familyName,
      jobTitle,
      domain,
      preferredUsername,
      email,
      emailVerified,
      locale,
      sites,
      banners,
      regions,
      fulfillmentContexts
    )

  }

}
