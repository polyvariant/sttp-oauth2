package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import io.circe.refined._
import sttp.client3.ResponseAs

import java.time.Instant

object Introspection {

  type Response = Either[common.Error, Introspection.TokenIntrospectionResponse]

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
    jti: Option[String] = None,
    aud: Option[Audience] = None
  )

  object TokenIntrospectionResponse {

    private implicit val instantDecoder: Decoder[Instant] = Decoder.decodeLong.map(Instant.ofEpochSecond)

    implicit val decoder: Decoder[TokenIntrospectionResponse] =
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

  }

  sealed trait Audience extends Product with Serializable

  final case class StringAudience(
    value: String
  ) extends Audience

  final case class SeqAudience(
    value: Seq[String]
  ) extends Audience

  object Audience {

    private val encoder: Encoder[Audience] = {
      case StringAudience(value) => Encoder.encodeString(value)
      case SeqAudience(value)    => Encoder.encodeSeq[String].apply(value)
    }

    private val decoder: Decoder[Audience] =
      Decoder.decodeString.map(StringAudience.apply).or(Decoder.decodeSeq[String].map(SeqAudience.apply))

    implicit val codec: Codec[Audience] = Codec.from(decoder, encoder)
  }

}
