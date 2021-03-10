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

  final case class TokenIntrospectionResponse(
    clientId: String,
    domain: String,
    exp: Instant,
    active: Boolean,
    authorities: List[String],
    scope: Scope,
    tokenType: String
  )

  object TokenIntrospectionResponse {

    implicit val instantDecoder: Decoder[Instant] = Decoder.decodeLong.map(Instant.ofEpochSecond)

    implicit val decoder: Decoder[TokenIntrospectionResponse] =
      Decoder.forProduct7(
        "client_id",
        "domain",
        "exp",
        "active",
        "authorities",
        "scope",
        "token_type"
      )(TokenIntrospectionResponse.apply)

  }

}
