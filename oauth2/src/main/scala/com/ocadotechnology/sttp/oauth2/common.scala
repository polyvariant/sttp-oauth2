package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidClient
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidGrant
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidRequest
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.InvalidScope
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.UnauthorizedClient
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2ErrorResponse.UnsupportedGrantType
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.api.Validate
import eu.timepit.refined.internal.RefineMPartiallyApplied
import io.circe.Decoder
import sttp.client3.ResponseAs
import sttp.client3.circe.asJson
import sttp.model.StatusCode
import eu.timepit.refined.string.Url
import sttp.model.Uri

object common {
  final case class ValidScope()

  object ValidScope {
    private val scopeRegex = """^(\x21|[\x23-\x5b]|[\x5d-\x7e])+(\s(\x21|[\x23-\x5b]|[\x5d-\x7e])+)*$"""

    implicit def scopeValidate: Validate.Plain[String, ValidScope] =
      Validate.fromPredicate(_.matches(scopeRegex), scope => s""""$scope" matches ValidScope""", ValidScope())
  }

  type Scope = String Refined ValidScope

  object Scope {
    def of(rawScope: String): Option[Scope] = refineV[ValidScope](rawScope).toOption

    def refine: RefineMPartiallyApplied[Refined, ValidScope] = refineMV[ValidScope]
  }

  sealed trait Error extends Throwable with Product with Serializable

  object Error {

    final case class HttpClientError(statusCode: StatusCode, cause: Throwable)
      extends Exception(s"Client call resulted in error ($statusCode): ${cause.getMessage}", cause)
      with Error

    sealed trait OAuth2Error extends Error

    /** Token errors as listed in documentation: https://tools.ietf.org/html/rfc6749#section-5.2
      */
    final case class OAuth2ErrorResponse(errorType: OAuth2ErrorResponse.OAuth2ErrorResponseType, errorDescription: String)
      extends Exception(s"$errorType: $errorDescription")
      with OAuth2Error

    object OAuth2ErrorResponse {

      sealed trait OAuth2ErrorResponseType extends Product with Serializable

      case object InvalidRequest extends OAuth2ErrorResponseType

      case object InvalidClient extends OAuth2ErrorResponseType

      case object InvalidGrant extends OAuth2ErrorResponseType

      case object UnauthorizedClient extends OAuth2ErrorResponseType

      case object UnsupportedGrantType extends OAuth2ErrorResponseType

      case object InvalidScope extends OAuth2ErrorResponseType

    }

    final case class UnknownOAuth2Error(error: String, description: String)
      extends Exception(s"Unknown OAuth2 error type: $error, description: $description")
      with OAuth2Error

    implicit val errorDecoder: Decoder[OAuth2Error] =
      Decoder.forProduct2[OAuth2Error, String, String]("error", "error_description") { (error, description) =>
        error match {
          case "invalid_request"        => OAuth2ErrorResponse(InvalidRequest, description)
          case "invalid_client"         => OAuth2ErrorResponse(InvalidClient, description)
          case "invalid_grant"          => OAuth2ErrorResponse(InvalidGrant, description)
          case "unauthorized_client"    => OAuth2ErrorResponse(UnauthorizedClient, description)
          case "unsupported_grant_type" => OAuth2ErrorResponse(UnsupportedGrantType, description)
          case "invalid_scope"          => OAuth2ErrorResponse(InvalidScope, description)
          case unknown                  => UnknownOAuth2Error(unknown, description)
        }
      }

  }

  private[oauth2] def responseWithCommonError[A](implicit decoder: Decoder[Either[OAuth2Error, A]]): ResponseAs[Either[Error, A], Any] =
    asJson[Either[OAuth2Error, A]].mapWithMetadata { case (either, meta) =>
      either match {
        case Left(sttpError) => Left(Error.HttpClientError(meta.code, sttpError))
        case Right(value)    => value
      }
    }

  final case class OAuth2Exception(error: Error) extends Exception(error.getMessage, error)

  final case class ParsingException(msg: String) extends Exception(msg)

  def refinedUrlToUri(url: String Refined Url): Uri =
    Uri.parse(url.toString).leftMap(e => throw ParsingException(e)).merge
}
