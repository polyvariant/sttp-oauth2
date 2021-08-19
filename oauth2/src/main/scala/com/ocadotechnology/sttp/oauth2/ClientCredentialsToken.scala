package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import io.circe.Decoder
import io.circe.refined._
import sttp.client3.ResponseAs
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import scala.concurrent.duration.FiniteDuration

object ClientCredentialsToken {

  type Response = Either[Error, ClientCredentialsToken.AccessTokenResponse]

  private[oauth2] implicit val bearerTokenResponseDecoder: Decoder[Either[OAuth2Error, AccessTokenResponse]] =
    circe.eitherOrFirstError[AccessTokenResponse, OAuth2Error](
      Decoder[AccessTokenResponse],
      Decoder[OAuth2Error]
    )

  val response: ResponseAs[Response, Any] =
    common.responseWithCommonError[ClientCredentialsToken.AccessTokenResponse]

  final case class AccessTokenResponse(
    accessToken: Secret[String],
    domain: Option[String],
    expiresIn: FiniteDuration,
    scope: Scope
  )

  object AccessTokenResponse {

    import com.ocadotechnology.sttp.oauth2.circe._

    implicit val tokenDecoder: Decoder[AccessTokenResponse] =
      Decoder
        .forProduct4(
          "access_token",
          "domain",
          "expires_in",
          "scope"
        )(AccessTokenResponse.apply)
        .validate {
          _.downField("token_type").as[String] match {
            case Right(value) if value.equalsIgnoreCase("Bearer") => List.empty
            case Right(string) => List(s"Error while decoding '.token_type': value '$string' is not equal to 'Bearer'")
            case Left(s)       => List(s"Error while decoding '.token_type': ${s.getMessage}")
          }
        }

  }

}
