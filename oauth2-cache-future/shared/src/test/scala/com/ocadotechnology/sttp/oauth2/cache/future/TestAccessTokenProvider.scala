package org.polyvariant.sttp.oauth2.cache.future

import org.polyvariant.sttp.oauth2.ClientCredentialsToken
import org.polyvariant.sttp.oauth2.common.Scope
import org.polyvariant.sttp.oauth2.AccessTokenProvider
import scala.concurrent.Future
import monix.execution.atomic.AtomicAny
import scala.concurrent.ExecutionContext

class TestAccessTokenProvider private (
  initial: Map[Option[Scope], ClientCredentialsToken.AccessTokenResponse]
)(
  implicit ec: ExecutionContext
) extends AccessTokenProvider[Future] {

  private val ref = AtomicAny(initial)

  def setToken(scope: Option[Scope], token: ClientCredentialsToken.AccessTokenResponse): Future[Unit] =
    Future.successful(ref.transform(_ + (scope -> token)))

  def requestToken(scope: Option[Scope]): Future[ClientCredentialsToken.AccessTokenResponse] =
    Future(ref.get().getOrElse(scope, throw new IllegalArgumentException(s"Unknown $scope")))
}

object TestAccessTokenProvider {

  def instance(
    initial: Map[Option[Scope], ClientCredentialsToken.AccessTokenResponse] = Map.empty
  )(
    implicit ec: ExecutionContext
  ): TestAccessTokenProvider = new TestAccessTokenProvider(initial)

}
