package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.common._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Url
import sttp.client3._
import sttp.model.Uri

/** Provides set of functions to simplify oauth2 identity provider integration.
  *  Use the `instance` companion object method to create instances.
  *
  * @tparam UriType type of returned uri. Supported types are:
  *                 Refined[String, Url] and Uri
  * @tparam F effect wrapper
  */
trait AuthorizationCodeProvider[UriType, F[_]] {

  /** Returns login link to oauth2 provider for user authentication
    *
    * Uses redirect link provided to instance constructor.
    *  @param state optional parameter, the state will be
    *               provided back to the service after oauth2 redirect
    *  @param scope set of Scope objects specifying access privileges
    *               @see https://tools.ietf.org/html/rfc6749#page-23
    *  @return instance of UriType, use to redirect user to Oauth2 login page
    */
  def loginLink(state: Option[String] = None, scope: Set[Scope] = Set.empty): UriType

  /** Returns logout link for to oauth2 provider
    *
    *  @param postLogoutRedirect Optional override of redirect link. By default uses login redirect link.
    *  @return instance of UriType, use to redirect user to Oauth2 logout page
    */
  def logoutLink(postLogoutRedirect: Option[UriType] = None): UriType

  /** Returns token details wrapped in effect
    *
    *  @param authCode code provided by oauth2 provider redirect,
    *                  after user is authenticated correctly
    *  @return Oauth2TokenResponse details containing user info and additional information
    */
  def authCodeToToken(authCode: String): F[Oauth2TokenResponse]

  /** Performs the token refresh on oauth2 provider nad returns new token details wrapped in effect
    *
    *  @param refreshToken value from refresh_token field of previous access token
    *  @param scope optional parameter for overriding token scope, useful to narrow down the scope
    *               when not provided or ScopeSelection.KeepExisting passed,
    *               the new token will be issued for the same scope as the previous one
    *  @return Oauth2TokenResponse details containing user info and additional information
    */
  def refreshAccessToken(refreshToken: String, scope: ScopeSelection = ScopeSelection.KeepExisting): F[Oauth2TokenResponse]
}

object AuthorizationCodeProvider {

  def apply[U, F[_]](implicit ev: AuthorizationCodeProvider[U, F]): AuthorizationCodeProvider[U, F] = ev

  /*
    Structure describing endpoints configuration for selected oauth2 provider
   */
  final case class Config(
    loginPath: Config.Path,
    logoutPath: Config.Path,
    tokenPath: Config.Path
  )

  object Config {

    case class Path(segments: List[Segment]) {
      def values: List[String] = segments.map(_.value)
    }

    case class Segment(value: String) extends AnyVal

    // Values chosen for backwards compatibilty
    val default = Config(
      loginPath = Path(List(Segment("oauth2"), Segment("login"))),
      logoutPath = Path(List(Segment("logout"))),
      tokenPath = Path(List(Segment("oauth2"), Segment("token")))
    )

    // Other predefined configurations for well-known oauth2 providers could be placed here
  }

  def refinedInstance[F[_]](
    baseUrl: Refined[String, Url],
    redirectUrl: Refined[String, Url],
    clientId: String,
    clientSecret: Secret[String],
    pathsConfig: Config = Config.default
  )(
    implicit backend: SttpBackend[F, Any]
  ): AuthorizationCodeProvider[Refined[String, Url], F] =
    new AuthorizationCodeProvider[Refined[String, Url], F] {

      private val baseUri = refinedUrlToUri(baseUrl)
      private val redirectUri = refinedUrlToUri(redirectUrl)
      private val tokenUri = baseUri.withPath(pathsConfig.tokenPath.values)

      override def loginLink(state: Option[String] = None, scope: Set[Scope] = Set.empty): Refined[String, Url] =
        refineV[Url].unsafeFrom[String](
          AuthorizationCode
            .loginLink(baseUri, redirectUri, clientId, state, scope)
            .toString
        )

      override def authCodeToToken(authCode: String): F[Oauth2TokenResponse] =
        AuthorizationCode
          .authCodeToToken(tokenUri, redirectUri, clientId, clientSecret, authCode)

      override def logoutLink(postLogoutRedirect: Option[Refined[String, Url]]): Refined[String, Url] =
        refineV[Url].unsafeFrom[String](
          AuthorizationCode
            .logoutLink(baseUri, redirectUri, clientId, postLogoutRedirect.map(refinedUrlToUri))
            .toString
        )

      override def refreshAccessToken(
        refreshToken: String,
        scopeOverride: ScopeSelection = ScopeSelection.KeepExisting
      ): F[Oauth2TokenResponse] =
        AuthorizationCode
          .refreshAccessToken(tokenUri, clientId, clientSecret, refreshToken, scopeOverride)

    }

  def uriInstance[F[_]](
    baseUrl: Uri,
    redirectUri: Uri,
    clientId: String,
    clientSecret: Secret[String],
    pathsConfig: Config = Config.default
  )(
    implicit backend: SttpBackend[F, Any]
  ): AuthorizationCodeProvider[Uri, F] =
    new AuthorizationCodeProvider[Uri, F] {
      private val tokenUri = baseUrl.withPath(pathsConfig.tokenPath.values)

      override def loginLink(state: Option[String] = None, scope: Set[Scope] = Set.empty): Uri =
        AuthorizationCode
          .loginLink(baseUrl, redirectUri, clientId, state, scope, pathsConfig.loginPath)

      override def authCodeToToken(authCode: String): F[Oauth2TokenResponse] =
        AuthorizationCode
          .authCodeToToken(tokenUri, redirectUri, clientId, clientSecret, authCode)

      override def logoutLink(postLogoutRedirect: Option[Uri]): Uri =
        AuthorizationCode
          .logoutLink(baseUrl, redirectUri, clientId, postLogoutRedirect, pathsConfig.logoutPath)

      override def refreshAccessToken(
        refreshToken: String,
        scopeOverride: ScopeSelection = ScopeSelection.KeepExisting
      ): F[Oauth2TokenResponse] =
        AuthorizationCode
          .refreshAccessToken(tokenUri, clientId, clientSecret, refreshToken, scopeOverride)

    }

}
