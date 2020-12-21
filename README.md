# sttp-oauth2 - OAuth2 client library for Scala

This library aims to provide easy integration with OAuth2 providers based on [OAuth2 RFC](https://tools.ietf.org/html/rfc6749) using [sttp](https://github.com/softwaremill/sttp) client. It uses [circe](https://github.com/circe/circe) for JSON serialization/deserialization.

Currently it supports methods (grant types) for obtaining authorization:
 - [Authorization code](https://tools.ietf.org/html/rfc6749#section-4.1)
 - [Password grant](https://tools.ietf.org/html/rfc6749#section-4.3)
 - [Client credentials](https://tools.ietf.org/html/rfc6749#section-4.4)


## Usage

Each grant is implemented in an object with explicit return and error types on methods and additionally, TaglessFinal friendly `*Provider` interface.
- `AuthorizationCode` and `AuthorizationCodeProvider` - provide functionality for: 
  - generating `login` and `logout` redirect links,
  - `authCodeToToken` for converting authorization code to token,
  - `refreshAccessToken` for performing a token refresh request
- `PasswordGrant` and `PasswordGrantProvider`, capable of performing `requestToken` to convert user login and password to oauth2 token
- `ClientCredentials` and `ClientCredentialsProvider` expose methods that:
  - Obtain token via `requestToken`
  - Verify token against application scope using `verify`
  - `introspect` the token for it's details like `UserInfo`

## Contributing

Feel free to submit feature requests and bug reports under Issues.

## License

sttp-oauth2 is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) (the "License"); you may not use this software except in compliance with the License.

Copyright © 2020 Ocado