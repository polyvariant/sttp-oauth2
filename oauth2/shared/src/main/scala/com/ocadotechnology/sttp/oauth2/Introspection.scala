package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import sttp.client3.ResponseAs

import java.time.Instant

object Introspection {

  type Response = Either[common.Error, Introspection.TokenIntrospectionResponse]

  def response(
    implicit decoder: JsonDecoder[TokenIntrospectionResponse],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): ResponseAs[Response, Any] =
    common.responseWithCommonError[TokenIntrospectionResponse]

  // Defined by https://datatracker.ietf.org/doc/html/rfc7662#section-2.2 with some extra fields
  final case class TokenIntrospectionResponse(
    active: Boolean,
    clientId: Option[String] = None,
    domain: Option[String] = None,
    exp: Option[Instant] = None,
    iat: Option[Instant] = None,
    nbf: Option[Instant] = None,
    authorities: Option[List[String]] = None,
    scope: Option[Scope] = None,
    tokenType: Option[String] = None,
    sub: Option[String] = None,
    iss: Option[String] = None,
    jti: Option[String] = None,
    aud: Option[Audience] = None
  )

  sealed trait Audience extends Product with Serializable

  final case class StringAudience(
    value: String
  ) extends Audience

  final case class SeqAudience(
    value: Seq[String]
  ) extends Audience

}
