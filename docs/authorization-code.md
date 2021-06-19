---
sidebar_position: 3
description: Authorization code grant documentation
---

# Authorization code grant

## Methods

`AuthorizationCode` and `AuthorizationCodeProvider` - provide functionality for: 
- generating _login_ and _logout_ redirect links,
- `authCodeToToken` for converting authorization code to token,
- `refreshAccessToken` for performing a token refresh request

## Token types

`authCodeToToken` and `refreshAccessToken` require `RT <: OAuth2TokenResponse.Basic: Decoder` type parameter, that describes desired. response structure. You can use `OAuth2TokenResponse`, `ExtendedOAuth2TokenResponse` or roll your own type that matches the type bounds.

## Configuration

OAuth2 doesn't precisely define urls for used for the process. Those differ by provider.
`AuthorizationCodeProvider.Config` provides a structure for configuring the endpoints. 
For login with GitHub you can use `AuthorizationCodeProvider.Config.GitHub`. Feel free to issue a PR if you want any other well-known provider supported.
