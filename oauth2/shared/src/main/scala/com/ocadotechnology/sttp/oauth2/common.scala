package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import com.ocadotechnology.sttp.oauth2.json.SttpJsonSupport.asJson
import eu.timepit.refined.api.Refined
import eu.timepit.refined.api.RefinedTypeOps
import eu.timepit.refined.api.Validate
import eu.timepit.refined.string.Url
import sttp.client3.DeserializationException
import sttp.client3.HttpError
import sttp.client3.ResponseAs
import sttp.model.StatusCode
import sttp.model.Uri

object common {
  final case class ValidScope()

  object ValidScope {
    private val scopeRegex = """^(\x21|[\x23-\x5b]|[\x5d-\x7e])+(\s(\x21|[\x23-\x5b]|[\x5d-\x7e])+)*$"""

    implicit def scopeValidate: Validate.Plain[String, ValidScope] =
      Validate.fromPredicate(_.matches(scopeRegex), scope => s""""$scope" matches ValidScope""", ValidScope())
  }

  type Scope = String Refined ValidScope

  object Scope extends RefinedTypeOps[Scope, String] {
    def of(rawScope: String): Option[Scope] = from(rawScope).toOption
  }

  sealed trait Error extends Throwable with Product with Serializable

  object Error {

    final case class HttpClientError(statusCode: StatusCode, cause: Throwable)
      extends Exception(s"Client call resulted in error ($statusCode): ${cause.getMessage}", cause)
      with Error

    sealed trait OAuth2Error extends Error

    /** Token errors as listed in documentation: https://tools.ietf.org/html/rfc6749#section-5.2
      */
    final case class OAuth2ErrorResponse(errorType: OAuth2ErrorResponse.OAuth2ErrorResponseType, errorDescription: Option[String])
      extends Exception(errorDescription.fold(s"$errorType")(description => s"$errorType: $description"))
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

    final case class UnknownOAuth2Error(error: String, errorDescription: Option[String])
      extends Exception(
        errorDescription.fold(s"Unknown OAuth2 error type: $error")(description =>
          s"Unknown OAuth2 error type: $error, description: $description"
        )
      )
      with OAuth2Error

  }

  private[oauth2] def responseWithCommonError[A](
    implicit decoder: JsonDecoder[A],
    oAuth2ErrorDecoder: JsonDecoder[OAuth2Error]
  ): ResponseAs[Either[Error, A], Any] =
    asJson[A].mapWithMetadata { case (either, meta) =>
      either match {
        case Left(HttpError(response, statusCode)) if statusCode.isClientError =>
          JsonDecoder[OAuth2Error]
            .decodeString(response)
            .fold(error => Error.HttpClientError(statusCode, DeserializationException(response, error)).asLeft[A], _.asLeft[A])
        case Left(sttpError)                                                   => Left(Error.HttpClientError(meta.code, sttpError))
        case Right(value)                                                      => value.asRight[Error]
      }
    }

  final case class OAuth2Exception(error: Error) extends Exception(error.getMessage, error)

  final case class ParsingException(msg: String) extends Exception(msg)

  def refinedUrlToUri(url: String Refined Url): Uri =
    Uri.parse(url.toString).leftMap(e => throw ParsingException(e)).merge
}
