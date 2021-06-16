package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.common._
import io.circe.parser.decode
import sttp.client3._
import sttp.model.Uri
import sttp.monad.MonadError
import sttp.monad.syntax._

import AuthorizationCodeProvider.Config
import sttp.model.HeaderNames

object AuthorizationCode {

  private def prepareLoginLink(
    baseUri: Uri,
    clientId: String,
    redirectUri: String,
    state: String,
    scopes: Set[Scope],
    path: List[String]
  ): Uri =
    baseUri
      .withPath(path)
      .addParam("response_type", "code")
      .addParam("client_id", clientId)
      .addParam("redirect_uri", redirectUri)
      .addParam("state", state)
      .addParam("scope", scopes.mkString(" "))

  private def prepareLogoutLink(baseUri: Uri, clientId: String, redirectUri: String, path: List[String]): Uri =
    baseUri
      .withPath(path)
      .addParam("client_id", clientId)
      .addParam("redirect_uri", redirectUri)

  private def convertAuthCodeToUser[F[_], UriType](
    tokenUri: Uri,
    authCode: String,
    redirectUri: String,
    clientId: String,
    clientSecret: Secret[String]
  )(
    implicit backend: SttpBackend[F, Any]
  ): F[Oauth2TokenResponse] = {
    implicit val F: MonadError[F] = backend.responseMonad
    backend
      .send {
        basicRequest
          .post(tokenUri)
          .body(tokenRequestParams(authCode, redirectUri, clientId, clientSecret.value))
          .response(asString)
          .header(HeaderNames.Accept, "application/json")
      }
      .map(_.body.leftMap(new RuntimeException(_)).flatMap(decode[Oauth2TokenResponse]).toTry)
      .flatMap(backend.responseMonad.fromTry)
  }

  private def tokenRequestParams(authCode: String, redirectUri: String, clientId: String, clientSecret: String) =
    Map(
      "grant_type" -> "authorization_code",
      "client_id" -> clientId,
      "client_secret" -> clientSecret,
      "redirect_uri" -> redirectUri,
      "code" -> authCode
    )

  private def performTokenRefresh[F[_], UriType](
    tokenUri: Uri,
    refreshToken: String,
    clientId: String,
    clientSecret: Secret[String],
    scopeOverride: ScopeSelection
  )(
    implicit backend: SttpBackend[F, Any]
  ): F[Oauth2TokenResponse] = {
    implicit val F: MonadError[F] = backend.responseMonad
    backend
      .send {
        basicRequest
          .post(tokenUri)
          .body(refreshTokenRequestParams(refreshToken, clientId, clientSecret.value, scopeOverride.toRequestMap))
          .response(asString)
      }
      .map(_.body.leftMap(new RuntimeException(_)).flatMap(decode[RefreshTokenResponse]).toTry)
      .map(_.map(_.toOauth2Token(refreshToken)))
      .flatMap(backend.responseMonad.fromTry)
  }

  private def refreshTokenRequestParams(refreshToken: String, clientId: String, clientSecret: String, scopeOverride: Map[String, String]) =
    Map(
      "grant_type" -> "refresh_token",
      "refresh_token" -> refreshToken,
      "client_id" -> clientId,
      "client_secret" -> clientSecret
    ) ++ scopeOverride

  def loginLink[F[_]](
    baseUrl: Uri,
    redirectUri: Uri,
    clientId: String,
    state: Option[String] = None,
    scopes: Set[Scope] = Set.empty,
    path: Config.Path = AuthorizationCodeProvider.Config.default.loginPath
  ): Uri =
    prepareLoginLink(baseUrl, clientId, redirectUri.toString, state.getOrElse(""), scopes, path.values)

  def authCodeToToken[F[_]](
    tokenUri: Uri,
    redirectUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    authCode: String
  )(
    implicit backend: SttpBackend[F, Any]
  ): F[Oauth2TokenResponse] =
    convertAuthCodeToUser(tokenUri, authCode, redirectUri.toString, clientId, clientSecret)

  def logoutLink[F[_]](
    baseUrl: Uri,
    redirectUri: Uri,
    clientId: String,
    postLogoutRedirect: Option[Uri] = None,
    path: Config.Path = AuthorizationCodeProvider.Config.default.logoutPath
  ): Uri =
    prepareLogoutLink(baseUrl, clientId, postLogoutRedirect.getOrElse(redirectUri).toString(), path.values)

  def refreshAccessToken[F[_]](
    tokenUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    refreshToken: String,
    scopeOverride: ScopeSelection = ScopeSelection.KeepExisting
  )(
    implicit backend: SttpBackend[F, Any]
  ): F[Oauth2TokenResponse] =
    performTokenRefresh(tokenUri, refreshToken, clientId, clientSecret, scopeOverride)

}
