---
sidebar_position: 8
description: Migrations
---

# Migrating to newer versions

Some releases introduce breaking changes. This page aims to list those and provide migration guide.

## [v0.17.0-RC-1](https://github.com/polyvariant/sttp-oauth2/releases/tag/v0.17.0)

Significant changes were introduced due to separation of JSON deserialisation from the core. Adding a module
with chosen JSON implementation is now required, as is importing an associated set of `JsonDecoder`s.

For backwards compatibility just add `circe` module:

```scala
"org.polyvariant" %% "sttp-oauth2-circe" % "0.16.0"
```

and a following import where you were using `sttp-oauth2`:

```scala
import org.polyvariant.sttp.oauth2.json.circe.instances._
```

## [v0.16.0](https://github.com/polyvariant/sttp-oauth2/releases/tag/v0.16.0)

Minor change [#336](https://github.com/polyvariant/sttp-oauth2/pull/336) removed implicit parameter
of `cats.MonadThrow` in some methods. As long as your code just uses these methods (doesn't override or mock
interfaces), you have to only solve warnings suggesting that there are unused parameters. Otherwise,
remove `: MonadError` from inherited implementations.

Affected classes: `PasswordGrant`, `PasswordGrantProvider`, `SttpOauth2ClientCredentialsBackend`, `UserInfoProvider`

## [v0.15.0](https://github.com/polyvariant/sttp-oauth2/releases/tag/v0.15.0)

### Breaking change in authorization code grant

In [#273](https://github.com/polyvariant/sttp-oauth2/pull/273) we have switched from using `withPath` to `addPath` in `AuthorizationCode` and `AuthorizationCodeProvider`.

If you were instantiating `AuthorizationCodeProvider` or using `AuthorizationCode` providing `baseUri` with path included, this would strip the path. Since `0.15.0` this is no longer the case. If you relied on this behavior, please remove the path from the provided URL before creating the instance.

## [v0.14.0](https://github.com/polyvariant/sttp-oauth2/releases/tag/v0.14.0)

Due to Scala 3 support `Scope.refine` Refined macro has been removed. Scope object now extends `RefinedTypeOps[Scope, String]`. 
To parse `Scope` use `Scope.of` or other functions provided by `RefinedTypeOps` - `from`, `unsafeFrom` or `unapply`. 

Since this version, scope is also made optional in [#199](https://github.com/polyvariant/sttp-oauth2/pull/199) to match the [spec](https://datatracker.ietf.org/doc/html/rfc6749#section-3.3).

## [v0.12.0](https://github.com/polyvariant/sttp-oauth2/releases/tag/v0.12.0)

### `SttpBackend` no more passed as implicit param

Applying `sttp` convention, not to pass `SttpBackend` as implicit param, all methods that require it (like constructor of `ClientCredentialsProvider`) have been changed to require this as explicit parameter.

Change

```scala
implicit val backend: SttpBackend[IO, Any] = ???
ClientCredentialsProvider.instance[IO](tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)
```

into:

```scala
val backend: SttpBackend[IO, Any] = ???
ClientCredentialsProvider[IO](tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)(backend)
```

### Split `ClientCredentialsProvider`

`ClientCredentialsProvider` has been split into `AccessTokenProvider` and `TokenIntrospection`. This allows using better scoped traits without a need to provide redundant token introspection url if there is only need for requesting access tokens. 

`ClientCredentialsProvider` has been left as a sum of both traits for smoother migration, so in most cases no changes would be required during the migration.

### Caching

In this release modules `oauth2-cache-xx` have been introduced, that contain cache based `AccessTokenProvider` for `cats-effect2` and `Future`. This has lead to removal of `SttpOauth2ClientCredentialsCatsBackend` and `SttpOauth2ClientCredentialsFutureBackend`. Instead a generic `SttpOauth2ClientCredentialsBackend` should be used with a `AccessTokenProvider` of your choice. 

To build cached `SttpBackend`:
- replace dependency of `sttp-oauth2-backend-xx` with `sttp-oauth2-cache-xx`
- replace creation of `SttpOauth2ClientCredentialsXXXBackend` with the following example adjusted to your needs:

```scala
val accessTokenProvider = AccessTokenProvider[IO](tokenUrl, clientId, clientSecret)(backend)
CachingAccessTokenProvider.refCacheInstance[IO](accessTokenProvider).map { cachingAccessTokenProvider => 
    SttpOauth2ClientCredentialsBackend[IO, Any](cachingAccessTokenProvider)(scope)
}
```

For details please see [PR](https://github.com/polyvariant/sttp-oauth2/pull/149).

### Apply

In many companion objects factory methods called `instance` have been replaced with `apply`, so previous way of creating objects:

```scala
ClientCredentialsProvider.instance[IO](tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)
```

needs to be replaced with:

```scala
ClientCredentialsProvider[IO](tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)
```


## [v0.10.0](https://github.com/polyvariant/sttp-oauth2/releases/tag/v0.5.0)

`authCodeToToken` and `refreshAccessToken` no longer return fixed token response type. Instead, they require `RT <: OAuth2TokenResponse.Basic: Decoder` type parameter, that describes desired. response structure.

There are two matching pre-defined types provided:
- `OAuth2TokenResponse` - minimal response as described by [rfc6749](https://datatracker.ietf.org/doc/html/rfc6749#section-5.1)
- `ExtendedOAuth2TokenResponse` - previously known as `Oauth2TokenResponse`, the previously fixed response type. Use this for backward compatiblity.

## [v0.5.0](https://github.com/polyvariant/sttp-oauth2/releases/tag/v0.5.0)

This version introduces [sttp3](https://github.com/polyvariant/sttp-oauth2/pull/39). Please see [sttp v3.0.0 release](https://github.com/softwaremill/sttp/releases/tag/v3.0.0) for migration guide.
