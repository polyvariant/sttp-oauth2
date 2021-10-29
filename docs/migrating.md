---
sidebar_position: 6
description: Migrations
---

# Migrating to newer versions

Some releases introduce breaking changes. This page aims to list those and provide migration guide.

## [unreleased](https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.12.0)

### Split `ClientCredentialsProvider`

`ClientCredentialsProvider` has been split into `AccessTokenProvider` and `TokenIntrospection`. This allows using better scoped traits without a need to provide redundant token introspection url if there is only need for requesting access tokens. 

`ClientCredentialsProvider` has been left as a sum of both traits for smoother migration, so in most cases no changes would be required during the migration.

## [v0.10.0](https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.5.0)

`authCodeToToken` and `refreshAccessToken` no longer return fixed token response type. Instead, they require `RT <: OAuth2TokenResponse.Basic: Decoder` type parameter, that describes desired. response structure.

There are two matching pre-defined types provided:
- `OAuth2TokenResponse` - minimal response as described by [rfc6749](https://datatracker.ietf.org/doc/html/rfc6749#section-5.1)
- `ExtendedOAuth2TokenResponse` - previously known as `Oauth2TokenResponse`, the previously fixed response type. Use this for backward compatiblity.

## [v0.5.0](https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.5.0)

This version introduces [sttp3](https://github.com/ocadotechnology/sttp-oauth2/pull/39). Please see [sttp v3.0.0 release](https://github.com/softwaremill/sttp/releases/tag/v3.0.0) for migration guide.