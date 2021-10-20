---
sidebar_position: 2
description: Client credentials grant documentation
---

# Client credentials grant

`ClientCredentials` and traits `AccessTokenProvider` and `TokenIntrospection` expose methods that:
- Obtain token via `requestToken`
- `introspect` the token for it's details like `UserInfo`


## `sttp-oauth2` backends

- provide Client Credentials Backend, which is an interceptor for another backend and which can:
  - fetch a token using `AccessTokenProvider`
  - reuse the token multiple times using a cache (default cache implementation may be overridden using appropriate constructor functions)
  - fetch a new token if the previous one expires
  - add an Authorization header to the intercepted request

Implementations:

| module name                  | class name                                 | default cache implementation    | semaphore                            | notes                                           |
|------------------------------|--------------------------------------------|---------------------------------|--------------------------------------|-------------------------------------------------|
| `sttp-oauth2-backend-cats`   | `SttpOauth2ClientCredentialsCatsBackend`   | `cats-effect`'s `Ref`           | `cats-effect`'s `Semaphore`          |                                                 |
| `sttp-oauth2-backend-future` | `SttpOauth2ClientCredentialsFutureBackend` | `monix-execution`'s `AtomicAny` | `monix-execution`'s `AsyncSemaphore` | It only uses submodule of whole `monix` project |
