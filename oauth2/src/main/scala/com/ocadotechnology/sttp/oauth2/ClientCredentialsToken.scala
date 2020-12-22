package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import io.circe.Decoder
import io.circe.generic.extras.semiauto._
import io.circe.refined._
import sttp.client.ResponseAs
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error

import scala.concurrent.duration.FiniteDuration

object ClientCredentialsToken {

  type Response = Either[Error, ClientCredentialsToken.AccessTokenResponse]

  private[oauth2] implicit val bearerTokenResponseDecoder: Decoder[Either[OAuth2Error, AccessTokenResponse]] =
    circe.eitherOrFirstError[AccessTokenResponse, OAuth2Error](
      Decoder[AccessTokenResponse],
      Decoder[OAuth2Error]
    )

  val response: ResponseAs[Response, Nothing] =
    common.responseWithCommonError[ClientCredentialsToken.AccessTokenResponse]

  final case class AccessTokenResponse(
    accessToken: Secret[String],
    domain: String,
    expiresIn: FiniteDuration,
    scope: Scope
  )

  object AccessTokenResponse {

    import com.ocadotechnology.sttp.oauth2.circe._

    private case object Bearer

    private implicit val bearerDecoder: Decoder[Bearer.type] =
      Decoder[String].emap(string =>
        if (string.equalsIgnoreCase("Bearer")) Bearer.asRight[String]
        else Left(s"Error while decoding '.token_type' value '$string' is not equal to 'Bearer'")
      )

    val tokenDecoder: Decoder[AccessTokenResponse] = deriveConfiguredDecoder

    implicit val decoder: Decoder[AccessTokenResponse] =
      Decoder[Bearer.type].prepare(_.downField("token_type")) >> tokenDecoder

  }

}
