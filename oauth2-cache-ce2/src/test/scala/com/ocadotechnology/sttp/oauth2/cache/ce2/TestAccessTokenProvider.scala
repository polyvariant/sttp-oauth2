package com.ocadotechnology.sttp.oauth2.cache.ce2

import cats.Functor
import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.ClientCredentialsToken
import com.ocadotechnology.sttp.oauth2.Introspection
import com.ocadotechnology.sttp.oauth2.Secret
import com.ocadotechnology.sttp.oauth2.common.Scope
import com.ocadotechnology.sttp.oauth2.AccessTokenProvider
import cats.effect.Ref

trait TestAccessTokenProvider[F[_]] extends AccessTokenProvider[F] {
  def setToken(scope: Option[Scope], token: ClientCredentialsToken.AccessTokenResponse): F[Unit]
}

object TestAccessTokenProvider {

  final case class State(
    tokens: Map[Option[Scope], ClientCredentialsToken.AccessTokenResponse],
    introspections: Map[Secret[String], Introspection.TokenIntrospectionResponse]
  )

  object State {
    val empty: State = State(Map.empty, Map.empty)
  }

  def apply[F[_]: Functor](ref: Ref[F, State]): TestAccessTokenProvider[F] =
    new TestAccessTokenProvider[F] {
      override def requestToken(scope: Option[Scope]): F[ClientCredentialsToken.AccessTokenResponse] =
        ref.get.map(_.tokens.getOrElse(scope, throw new IllegalArgumentException(s"Unknown $scope")))

      override def setToken(scope: Option[Scope], token: ClientCredentialsToken.AccessTokenResponse): F[Unit] =
        ref.update(state => state.copy(tokens = state.tokens + (scope -> token)))
    }

}
