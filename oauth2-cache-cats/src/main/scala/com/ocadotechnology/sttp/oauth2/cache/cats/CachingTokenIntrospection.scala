package com.ocadotechnology.sttp.oauth2.cache.cats

import cats.data.OptionT
import cats.effect.kernel.Clock
import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.TokenIntrospection
import com.ocadotechnology.sttp.oauth2.cache.ExpiringCache

import java.time.Instant
import scala.concurrent.duration._

final class CachingTokenIntrospection[F[_]: Clock: MonadCancelThrow](
  delegate: TokenIntrospection[F],
  cache: ExpiringCache[F, Secret[String], TokenIntrospectionResponse],
  defaultTimeToLive: FiniteDuration
) extends TokenIntrospection[F] {

  override def introspect(
    token: Secret[String]
  ): F[TokenIntrospectionResponse] =
    getFromCache(token).flatMap {
      case Some(value) => value.pure[F]
      case None        => fetchAndCache(token)
    }

  private def getFromCache(
    token: Secret[String]
  ): F[Option[TokenIntrospectionResponse]] = {
    for {
      now      <- OptionT.liftF(Clock[F].realTime) // Using realTime since it's available cross platform
      response <- OptionT(cache.get(token))
      result   <- OptionT.when[F, TokenIntrospectionResponse](responseIsUpToDate(now, response))(response)
    } yield result
  }.value

  private def fetchAndCache(
    token: Secret[String]
  ): F[TokenIntrospectionResponse] =
    for {
      now    <- Clock[F].realTime
      nowInstant = Instant.ofEpochMilli(now.toMillis)
      result <- delegate
                  .introspect(token)
                  .flatTap { response =>
                    cache.put(token, response, responseExpirationOrDefault(nowInstant, response))
                  }
    } yield result

  private def responseIsUpToDate(
    now: FiniteDuration,
    response: TokenIntrospectionResponse
  ): Boolean =
    response.exp.map(_.toEpochMilli > now.toMillis).getOrElse(true)

  private def responseExpirationOrDefault(
    now: Instant,
    response: TokenIntrospectionResponse
  ): Instant = {
    val defaultExpirationTime = now.plusNanos(defaultTimeToLive.toNanos)
    response
      .exp
      .filter(_.isBefore(defaultExpirationTime))
      .getOrElse(defaultExpirationTime)
  }

}

object CachingTokenIntrospection {

  def apply[F[_]: Clock: MonadCancelThrow](
    delegate: TokenIntrospection[F],
    cache: ExpiringCache[F, Secret[String], TokenIntrospectionResponse],
    defaultTimeToLive: FiniteDuration
  ): CachingTokenIntrospection[F] =
    new CachingTokenIntrospection[F](delegate, cache, defaultTimeToLive)

}
