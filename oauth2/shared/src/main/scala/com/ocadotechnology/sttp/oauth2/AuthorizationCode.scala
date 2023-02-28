package com.ocadotechnology.sttp.oauth2

import cats.implicits._
import com.ocadotechnology.sttp.oauth2.common._
import com.ocadotechnology.sttp.oauth2.AuthorizationCodeProvider.Config
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import sttp.client3._
import sttp.model.HeaderNames
import sttp.model.Uri
import sttp.monad.MonadError
import sttp.monad.syntax._

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
      .addPath(path)
      .addParam("response_type", "code")
      .addParam("client_id", clientId)
      .addParam("redirect_uri", redirectUri)
      .addParam("state", state)
      .addParam("scope", scopes.mkString(" "))

  private def prepareLogoutLink(
    baseUri: Uri,
    clientId: String,
    redirectUri: String,
    path: List[String]
  ): Uri =
    baseUri
      .addPath(path)
      .addParam("client_id", clientId)
      .addParam("redirect_uri", redirectUri)

  private def convertAuthCodeToUser[F[_], UriType, RT <: OAuth2TokenResponse.Basic: JsonDecoder](
    tokenUri: Uri,
    authCode: String,
    redirectUri: String,
    clientId: String,
    clientSecret: Secret[String]
  )(
    backend: SttpBackend[F, Any]
  ): F[RT] = {
    implicit val F: MonadError[F] = backend.responseMonad
    backend
      .send {
        basicRequest
          .post(tokenUri)
          .body(tokenRequestParams(authCode, redirectUri, clientId, clientSecret.value))
          .response(asString)
          .header(HeaderNames.Accept, "application/json")
      }
      .map(_.body.leftMap(new RuntimeException(_)).flatMap(JsonDecoder[RT].decodeString))
      .flatMap(_.fold(F.error, F.unit))
  }

  private def tokenRequestParams(
    authCode: String,
    redirectUri: String,
    clientId: String,
    clientSecret: String
  ) =
    Map(
      "grant_type" -> "authorization_code",
      "client_id" -> clientId,
      "client_secret" -> clientSecret,
      "redirect_uri" -> redirectUri,
      "code" -> authCode
    )

  private def performTokenRefresh[F[_], UriType, RT <: OAuth2TokenResponse.Basic: JsonDecoder](
    tokenUri: Uri,
    refreshToken: String,
    clientId: String,
    clientSecret: Secret[String],
    scopeOverride: ScopeSelection
  )(
    backend: SttpBackend[F, Any]
  ): F[RT] = {
    implicit val F: MonadError[F] = backend.responseMonad
    backend
      .send {
        basicRequest
          .post(tokenUri)
          .body(refreshTokenRequestParams(refreshToken, clientId, clientSecret.value, scopeOverride.toRequestMap))
          .response(asString)
      }
      .map(_.body.leftMap(new RuntimeException(_)).flatMap(JsonDecoder[RT].decodeString))
      .flatMap(_.fold(F.error, F.unit))
  }

  private def refreshTokenRequestParams(
    refreshToken: String,
    clientId: String,
    clientSecret: String,
    scopeOverride: Map[String, String]
  ) =
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

  def authCodeToToken[F[_], RT <: OAuth2TokenResponse.Basic: JsonDecoder](
    tokenUri: Uri,
    redirectUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    authCode: String
  )(
    backend: SttpBackend[F, Any]
  ): F[RT] =
    convertAuthCodeToUser[F, Uri, RT](tokenUri, authCode, redirectUri.toString, clientId, clientSecret)(backend)

  def logoutLink[F[_]](
    baseUrl: Uri,
    redirectUri: Uri,
    clientId: String,
    postLogoutRedirect: Option[Uri] = None,
    path: Config.Path = AuthorizationCodeProvider.Config.default.logoutPath
  ): Uri =
    prepareLogoutLink(baseUrl, clientId, postLogoutRedirect.getOrElse(redirectUri).toString(), path.values)

  def refreshAccessToken[F[_], RT <: OAuth2TokenResponse.Basic: JsonDecoder](
    tokenUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    refreshToken: String,
    scopeOverride: ScopeSelection = ScopeSelection.KeepExisting
  )(
    backend: SttpBackend[F, Any]
  ): F[RT] =
    performTokenRefresh[F, Uri, RT](tokenUri, refreshToken, clientId, clientSecret, scopeOverride)(backend)

}
