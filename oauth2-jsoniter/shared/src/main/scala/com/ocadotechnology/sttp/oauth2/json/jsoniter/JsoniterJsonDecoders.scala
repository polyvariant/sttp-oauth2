package com.ocadotechnology.sttp.oauth2.json.jsoniter

import cats.syntax.all._
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken.AccessTokenResponse
import com.ocadotechnology.sttp.oauth2.ExtendedOAuth2TokenResponse
import com.ocadotechnology.sttp.oauth2.Introspection.Audience
import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.OAuth2TokenResponse
import com.ocadotechnology.sttp.oauth2.RefreshTokenResponse
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.TokenUserDetails
import com.ocadotechnology.sttp.oauth2.UserInfo
import com.ocadotechnology.sttp.oauth2.common.Error.OAuth2Error
import com.ocadotechnology.sttp.oauth2.Introspection.SeqAudience
import com.ocadotechnology.sttp.oauth2.Introspection.StringAudience
import com.ocadotechnology.sttp.oauth2.common.Scope
import com.ocadotechnology.sttp.oauth2.json.jsoniter.JsoniterJsonDecoders.oAuth2ErrorHelperDecoder
import com.ocadotechnology.sttp.oauth2.json.jsoniter.JsoniterJsonDecoders.tokenTypeDecoder
import com.ocadotechnology.sttp.oauth2.json.jsoniter.JsoniterJsonDecoders.IntermediateOAuth2Error

import java.time.Instant
import scala.concurrent.duration.DurationLong
import scala.concurrent.duration.FiniteDuration
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.control.NonFatal

trait JsoniterJsonDecoders {

  implicit def jsonDecoder[A](implicit jsonCodec: JsonValueCodec[A]): JsonDecoder[A] =
    (data: String) =>
      Try(readFromString[A](data)) match {
        case Success(value)                    =>
          Right(value)
        case Failure(error: JsonDecoder.Error) =>
          Left(error)
        case Failure(NonFatal(throwable))      =>
          Left(JsonDecoder.Error.fromThrowable(throwable))
        case Failure(fatal)                    =>
          throw fatal
      }

  private[jsoniter] implicit val secondsDecoder: JsonValueCodec[FiniteDuration] = customDecoderFromUnsafe[FiniteDuration] { reader =>
    reader.readLong().seconds
  }

  private[jsoniter] implicit val instantCodec: JsonValueCodec[Instant] = customDecoderFromUnsafe[Instant] { reader =>
    Instant.ofEpochSecond(reader.readLong())
  }

  private[jsoniter] implicit val secretDecoder: JsonValueCodec[Secret[String]] = customDecoderFromUnsafe[Secret[String]] { reader =>
    Secret(reader.readString(default = null))
  }

  private[jsoniter] implicit val scopeDecoder: JsonValueCodec[Scope] = customDecoderWithDefault[Scope] { reader =>
    Try {
      reader.readString(default = null)
    }.flatMap { value =>
      Scope.of(value).toRight(JsonDecoder.Error(s"$value is not a valid $Scope")).toTry
    }
  }(Scope.of("default").get)

  private val stringSequenceCodec: JsonValueCodec[List[String]] = JsonCodecMaker.make

  private[jsoniter] implicit val audienceDecoder: JsonValueCodec[Audience] = customDecoderTry[Audience] { jsonReader =>
    Try {
      jsonReader.setMark()
      StringAudience(jsonReader.readString(default = null))
    } orElse Try {
      jsonReader.rollbackToMark()
      SeqAudience(stringSequenceCodec.decodeValue(jsonReader, default = null))
    }
  }

  private val tokenDecoderWithoutTypeValidation: JsonValueCodec[AccessTokenResponse] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

  implicit val tokenDecoderWithTypeValidation: JsonValueCodec[AccessTokenResponse] = customDecoderFromUnsafe[AccessTokenResponse] { in =>
    in.setMark()
    val tokenType = tokenTypeDecoder.decodeValue(in, tokenTypeDecoder.nullValue)
    if (tokenType.tokenType === "Bearer") {
      in.rollbackToMark()
      tokenDecoderWithoutTypeValidation.decodeValue(in, tokenDecoderWithoutTypeValidation.nullValue)
    } else {
      throw JsonDecoder.Error(s"Error while decoding '.token_type': value '$tokenType' is not equal to 'Bearer'")
    }
  }

  implicit val errorDecoder: JsonValueCodec[OAuth2Error] = customDecoderFromUnsafe[OAuth2Error] { in =>
    val IntermediateOAuth2Error(error, description) = oAuth2ErrorHelperDecoder.decodeValue(in, null)

    OAuth2Error.fromErrorTypeAndDescription(error, description)
  }

  implicit val userInfoDecoder: JsonValueCodec[UserInfo] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

  implicit val tokenResponseDecoder: JsonValueCodec[OAuth2TokenResponse] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

  implicit val tokenUserDetailsDecoder: JsonValueCodec[TokenUserDetails] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

  implicit val extendedTokenResponseDecoder: JsonValueCodec[ExtendedOAuth2TokenResponse] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

  implicit val tokenIntrospectionResponseDecoder: JsonValueCodec[TokenIntrospectionResponse] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

  implicit val refreshTokenResponseDecoder: JsonValueCodec[RefreshTokenResponse] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

  private def customDecoderFromUnsafe[A](read: JsonReader => A)(implicit toNull: Null <:< A): JsonValueCodec[A] =
    customDecoderTry[A](reader => Try(read(reader)))

  private def customDecoderTry[A](read: JsonReader => Try[A])(implicit toNull: Null <:< A): JsonValueCodec[A] =
    customDecoderWithDefault[A](read)(toNull(null))

  private def customDecoderWithDefault[A](read: JsonReader => Try[A])(default: A) = new JsonValueCodec[A] {

    override def decodeValue(reader: JsonReader, default: A): A =
      read(reader).get

    override def encodeValue(x: A, out: JsonWriter): Unit = throw JsonDecoder.Error("Tried to encode a value using a decoder 🤷")

    override def nullValue: A = default
  }

}

object JsoniterJsonDecoders {

  private case class TokenType(tokenType: String)

  private implicit val tokenTypeDecoder: JsonValueCodec[TokenType] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

  private case class IntermediateOAuth2Error(error: String, errorDescription: Option[String])

  private implicit val oAuth2ErrorHelperDecoder: JsonValueCodec[IntermediateOAuth2Error] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

}
