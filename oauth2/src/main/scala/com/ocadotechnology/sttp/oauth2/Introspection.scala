package com.ocadotechnology.sttp.oauth2

import java.time.Instant
import com.ocadotechnology.sttp.oauth2.common._
import io.circe.Decoder
import io.circe.refined._
import sttp.client3.ResponseAs
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error

object Introspection {

  type Response = Either[common.Error, Introspection.TokenIntrospectionResponse]

  private implicit val bearerTokenResponseDecoder: Decoder[Either[OAuth2Error, TokenIntrospectionResponse]] =
    circe.eitherOrFirstError[TokenIntrospectionResponse, OAuth2Error](
      Decoder[TokenIntrospectionResponse],
      Decoder[OAuth2Error]
    )

  val response: ResponseAs[Response, Any] =
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
    jti: Option[String] = None
    // aud is missing, not sure how to encode String or Seq[String] at the moment
  )

  object TokenIntrospectionResponse {

    private implicit val instantDecoder: Decoder[Instant] = Decoder.decodeLong.map(Instant.ofEpochSecond)

    implicit val decoder: Decoder[TokenIntrospectionResponse] =
      Decoder.forProduct12(
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
        "jti"
      )(TokenIntrospectionResponse.apply)

  }

}
