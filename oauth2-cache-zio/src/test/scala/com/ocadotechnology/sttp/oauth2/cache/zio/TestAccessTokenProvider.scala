package com.ocadotechnology.sttp.oauth2.cache.zio

import com.ocadotechnology.sttp.oauth2.common.Scope
import com.ocadotechnology.sttp.oauth2.{AccessTokenProvider, ClientCredentialsToken, Introspection, Secret}
import zio.{Ref, Task}

trait TestAccessTokenProvider extends AccessTokenProvider[Task] {
  def setToken(scope: Option[Scope], token: ClientCredentialsToken.AccessTokenResponse): Task[Unit]
}

object TestAccessTokenProvider {

  final case class State(
    tokens: Map[Option[Scope], ClientCredentialsToken.AccessTokenResponse],
    introspections: Map[Secret[String], Introspection.TokenIntrospectionResponse]
  )

  object State {
    val empty: State = State(Map.empty, Map.empty)
  }

  def apply(ref: Ref[State]): TestAccessTokenProvider =
    new TestAccessTokenProvider {
      override def requestToken(scope: Option[Scope]): Task[ClientCredentialsToken.AccessTokenResponse] =
        ref.get.map(_.tokens.getOrElse(scope, throw new IllegalArgumentException(s"Unknown $scope")))

      override def setToken(scope: Option[Scope], token: ClientCredentialsToken.AccessTokenResponse): Task[Unit] =
        ref.update(state => state.copy(tokens = state.tokens + (scope -> token)))
    }

}
