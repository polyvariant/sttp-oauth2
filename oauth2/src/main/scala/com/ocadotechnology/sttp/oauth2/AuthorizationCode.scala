package com.ocadotechnology.sttp.oauth2

import cats.MonadError
import cats.syntax.all._
import sttp.model.Uri
import sttp.client._
import io.circe.parser.decode
import com.ocadotechnology.sttp.oauth2.common._

object AuthorizationCode {

  private def prepareLoginLink(baseUri: Uri, clientId: String, redirectUri: String, state: String, scopes: Set[Scope]): Uri =
    baseUri
      .path(baseUri.path :+ "login")
      .param("response_type", "code")
      .param("client_id", clientId)
      .param("redirect_uri", redirectUri)
      .param("state", state)
      .param("scope", scopes.mkString(" "))

  private def prepareLogoutLink(baseUri: Uri, clientId: String, redirectUri: String): Uri =
    baseUri.path("logout").param("client_id", clientId).param("redirect_uri", redirectUri)

  private def convertAuthCodeToUser[F[_]: MonadError[*[_], Throwable], UriType](
    tokenUri: Uri,
    authCode: String,
    redirectUri: String,
    clientId: String,
    clientSecret: Secret[String]
  )(
    implicit backend: SttpBackend[F, Nothing, NothingT]
  ): F[Oauth2TokenResponse] =
    basicRequest
      .post(tokenUri)
      .body(tokenRequestParams(authCode, redirectUri, clientId, clientSecret.value))
      .response(asString)
      .send()
      .map(_.body.leftMap(new RuntimeException(_)).flatMap(decode[Oauth2TokenResponse]))
      .rethrow

  private def tokenRequestParams(authCode: String, redirectUri: String, clientId: String, clientSecret: String) =
    Map(
      "grant_type" -> "authorization_code",
      "client_id" -> clientId,
      "client_secret" -> clientSecret,
      "redirect_uri" -> redirectUri,
      "code" -> authCode
    )

  private def performTokenRefresh[F[_]: MonadError[*[_], Throwable], UriType](
    tokenUri: Uri,
    refreshToken: String,
    clientId: String,
    clientSecret: Secret[String],
    scopeOverride: ScopeSelection
  )(
    implicit backend: SttpBackend[F, Nothing, NothingT]
  ): F[Oauth2TokenResponse] =
    basicRequest
      .post(tokenUri)
      .body(refreshTokenRequestParams(refreshToken, clientId, clientSecret.value, scopeOverride.toRequestMap))
      .response(asString)
      .send()
      .map(
        _.body.leftMap(new RuntimeException(_)).flatMap(decode[RefreshTokenResponse])
      )
      .map(_.map(_.toOauth2Token(refreshToken)))
      .rethrow

  private def refreshTokenRequestParams(refreshToken: String, clientId: String, clientSecret: String, scopeOverride: Map[String, String]) =
    Map(
      "grant_type" -> "refresh_token",
      "refresh_token" -> refreshToken,
      "client_id" -> clientId,
      "client_secret" -> clientSecret
    ) ++ scopeOverride


  def loginLink[F[_]: MonadError[*[_], Throwable]](
    baseUrl: Uri,
    redirectUri: Uri,
    clientId: String,
    state: Option[String] = None,
    scopes: Set[Scope] = Set.empty
  ): Uri =
    prepareLoginLink(baseUrl, clientId, redirectUri.toString, state.getOrElse(""), scopes)

  def authCodeToToken[F[_]: MonadError[*[_], Throwable]](
    tokenUri: Uri,
    redirectUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    authCode: String
  )(
    implicit backend: SttpBackend[F, Nothing, NothingT]
  ): F[Oauth2TokenResponse] =
    convertAuthCodeToUser(tokenUri, authCode, redirectUri.toString, clientId, clientSecret)

  def logoutLink[F[_]: MonadError[*[_], Throwable]](
    baseUrl: Uri,
    redirectUri: Uri,
    clientId: String,
    postLogoutRedirect: Option[Uri]
  ): Uri =
    prepareLogoutLink(baseUrl, clientId, postLogoutRedirect.getOrElse(redirectUri).toString())

  def refreshAccessToken[F[_]: MonadError[*[_], Throwable]](
    tokenUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    refreshToken: String,
    scopeOverride: ScopeSelection = ScopeSelection.KeepExisting
  )(
    implicit backend: SttpBackend[F, Nothing, NothingT]
  ): F[Oauth2TokenResponse] =
    performTokenRefresh(tokenUri, refreshToken, clientId, clientSecret, scopeOverride)


}
