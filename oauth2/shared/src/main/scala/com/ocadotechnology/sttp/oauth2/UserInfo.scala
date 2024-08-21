package org.polyvariant.sttp.oauth2

/** Models user info as defined in open id standard
  * @see
  *   https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
  *
  * @param sub
  *   Subject - Identifier for the End-User at the Issuer
  * @param name
  *   End-User's full name
  * @param givenName
  *   Given name(s) or first name(s) of the End-User
  * @param familyName
  *   Surname(s) or last name(s) of the End-User
  * @param jobTitle
  *   Job Title of the End-User.
  * @param domain
  *   Domain of the End-User
  * @param preferredUsername
  *   Username of the End-User
  * @param email
  *   End-User's preferred e-mail address
  * @param emailVerified
  *   True if the End-User's e-mail address has been verified; otherwise false
  * @param locale
  *   Locale of the End-User
  * @param sites
  *   List of sites of the End-User.
  * @param banners
  *   List of banners End-User has access to.
  * @param regions
  *   List of regions End-User has access to.
  * @param fulfillmentContexts
  *   List of fulfillment contexts End-User has access to.
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
