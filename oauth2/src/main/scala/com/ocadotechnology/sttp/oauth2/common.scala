package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.common.Error.{OAuth2Error, errorDecoder}
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
import io.circe.parser.decode
import sttp.client3.ResponseAs
import sttp.client3.circe.asJson
import sttp.model.StatusCode
import eu.timepit.refined.string.Url
import sttp.model.Uri
import sttp.client3.HttpError
import sttp.client3.DeserializationException

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

  final case class OAuth2Exception[E <: Throwable](inner: Error[E]) extends Exception(s"${inner.message}: ${inner.error.getMessage}", inner.error)

  sealed trait Error[E] extends Product with Serializable {
    def error: E
    def message: String
  }

  object Error {

    final case class HttpClientError(statusCode: StatusCode, error: Throwable) extends Error[Throwable] {
      val message: String = s"Client call resulted in error ($statusCode)"
    }

    sealed trait OAuth2Error[E] extends Error[E]

    /** Token errors as listed in documentation: https://tools.ietf.org/html/rfc6749#section-5.2
      */
    final case class OAuth2ErrorResponse[E](
      errorType: OAuth2ErrorResponse.OAuth2ErrorResponseType,
      errorDescription: Option[String],
      error: E
    ) extends OAuth2Error[E] {
      val message: String = errorDescription.fold(s"$errorType")(description => s"$errorType: $description")
    }

    object OAuth2ErrorResponse {

      sealed trait OAuth2ErrorResponseType extends Product with Serializable

      case object InvalidRequest extends OAuth2ErrorResponseType

      case object InvalidClient extends OAuth2ErrorResponseType

      case object InvalidGrant extends OAuth2ErrorResponseType

      case object UnauthorizedClient extends OAuth2ErrorResponseType

      case object UnsupportedGrantType extends OAuth2ErrorResponseType

      case object InvalidScope extends OAuth2ErrorResponseType

    }

    final case class UnknownOAuth2Error[E](errorString: String, errorDescription: Option[String], error: E)
      extends OAuth2Error[E] {

      val message: String = errorDescription.fold(s"Unknown OAuth2 error type: $errorString")(description =>
        s"Unknown OAuth2 error type: $errorString, description: $description"
      )

    }

    def errorDecoder[E](cause: E): Decoder[OAuth2Error[E]] =
      Decoder.forProduct2[OAuth2Error[E], String, Option[String]]("error", "error_description") { (error, description) =>
        error match {
          case "invalid_request"        => OAuth2ErrorResponse(InvalidRequest, description, cause)
          case "invalid_client"         => OAuth2ErrorResponse(InvalidClient, description, cause)
          case "invalid_grant"          => OAuth2ErrorResponse(InvalidGrant, description, cause)
          case "unauthorized_client"    => OAuth2ErrorResponse(UnauthorizedClient, description, cause)
          case "unsupported_grant_type" => OAuth2ErrorResponse(UnsupportedGrantType, description, cause)
          case "invalid_scope"          => OAuth2ErrorResponse(InvalidScope, description, cause)
          case unknown                  => UnknownOAuth2Error(unknown, description, cause)
        }
      }

  }

  private[oauth2] def responseWithCommonError[A](implicit decoder: Decoder[A]): ResponseAs[Either[Error[Throwable], A], Any] =
    asJson[A].mapWithMetadata {
      case (either, meta) =>
        either match {
          case Left(HttpError(response, statusCode)) if statusCode.isClientError =>
            decode[OAuth2Error[Throwable]](response)(errorDecoder(HttpError(response, statusCode)))
              .fold(error => Error.HttpClientError(statusCode, DeserializationException(response, error)).asLeft[A], _.asLeft[A])
          case Left(sttpError)                                                           => Left(Error.HttpClientError(meta.code, sttpError))
          case Right(value)                                                              => value.asRight[Error[Throwable]]
        }
    }

  final implicit class ErrorToException[E <: Throwable](error: Error[E]) {
    def toException: Throwable = OAuth2Exception(error)
  }

  final case class ParsingException(msg: String) extends Exception(msg)

  def refinedUrlToUri(url: String Refined Url): Uri =
    Uri.parse(url.toString).leftMap(e => throw ParsingException(e)).merge
}
