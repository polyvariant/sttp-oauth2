package com.ocadotechnology.sttp.oauth2

import io.circe.Decoder
import cats.syntax.all._
import scala.concurrent.duration.DurationLong
import scala.concurrent.duration.FiniteDuration

object circe {

  implicit val decoderSeconds: Decoder[FiniteDuration] = Decoder.decodeLong.map(_.seconds)

  def eitherOrFirstError[A, B](aDecoder: Decoder[A], bDecoder: Decoder[B]): Decoder[Either[B, A]] =
    aDecoder.attempt.flatMap {
      case Right(a) => Decoder.const(a.asRight[B])
      case Left(e)  => bDecoder.either(Decoder.failed(e))
    }

}
