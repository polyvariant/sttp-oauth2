package com.ocadotechnology.sttp.oauth2

import scala.concurrent.duration.FiniteDuration

case class OAuth2TokenResponse(
  accessToken: Secret[String],
  scope: String,
  tokenType: String,
  expiresIn: Option[FiniteDuration],
  refreshToken: Option[String]
) extends OAuth2TokenResponse.Basic

object OAuth2TokenResponse {

  /** Miminal structure as required by RFC https://datatracker.ietf.org/doc/html/rfc6749#section-5.1 Token response is described in
    * https://datatracker.ietf.org/doc/html/rfc6749#section-5.1 as follows: access_token REQUIRED. The access token issued by the
    * authorization server.
    *
    * token_type REQUIRED. The type of the token issued as described in Section 7.1. Value is case insensitive.
    *
    * expires_in RECOMMENDED. The lifetime in seconds of the access token. For example, the value "3600" denotes that the access token will
    * expire in one hour from the time the response was generated. If omitted, the authorization server SHOULD provide the expiration time
    * via other means or document the default value.
    *
    * refresh_token OPTIONAL. The refresh token, which can be used to obtain new access tokens using the same authorization grant as
    * described in Section 6.
    *
    * scope OPTIONAL, if identical to the scope requested by the client; otherwise, REQUIRED. The scope of the access token as described by
    * Section 3.3.
    */
  trait Basic {
    def accessToken: Secret[String]
    def tokenType: String
  }

}

// @deprecated("This model will be removed in next release", "0.10.0")
case class ExtendedOAuth2TokenResponse(
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
) extends OAuth2TokenResponse.Basic
