---
sidebar_position: 2
description: Client credentials grant documentation
---

# Client credentials grant

`ClientCredentials` and traits `AccessTokenProvider` and `TokenIntrospection` expose methods that:
- Obtain token via `requestToken`
- `introspect` the token for it's details like `UserInfo`

```scala
val accessTokenProvider = AccessTokenProvider[IO](tokenUrl, clientId, clientSecret)
val tokenIntrospection = TokenIntrospection[IO](tokenIntrospectionUrl, clientId, clientSecret)
  
for {
  token <- accessTokenProvider.requestToken(scope) // ask for token
  response <- tokenIntrospection.introspect(token.accessToken) // check if token is valid
} yield response.active // is the token active?
```


## Caching

Caching modules provide cached `AccessTokenProvider`, which can:
  - reuse the token multiple times using a cache (default cache implementation may be overridden using appropriate constructor functions)
  - fetch a new token if the previous one expires


| module name                  | class name                                 | default cache implementation    | semaphore                            | notes                                           |
|------------------------------|--------------------------------------------|---------------------------------|--------------------------------------|-------------------------------------------------|
| `sttp-oauth2-cache-ce2`   | `SttpOauth2ClientCredentialsCatsBackend`   | `cats-effect2`'s `Ref`           | `cats-effect2`'s `Semaphore`          |                                                 |
| `sttp-oauth2-cache-future` | `SttpOauth2ClientCredentialsFutureBackend` | `monix-execution`'s `AtomicAny` | `monix-execution`'s `AsyncSemaphore` | It only uses submodule of whole `monix` project |

### Cats example

```scala
val delegate = AccessTokenProvider[IO](tokenUrl, clientId, clientSecret)
CachingAccessTokenProvider.refCacheInstance[IO](delegate)
```

## `sttp-oauth2` backends

`SttpOauth2ClientCredentialsBackend` is a `SttpBackend` which sends auth bearer headers for every `http` call performed with it using provided `AccessTokenProvider`.


```scala
val scope: Scope = "scope" // backend will use defined scope for all requests
val backend: SttpBackend[IO, Any] = SttpOauth2ClientCredentialsBackend[IO, Any](tokenUrl, clientId, clientSecret)(scope)
backend.send(request) // this will add header: Authorization: Bearer {token}

```

In order to cache tokens, use one of the `AccessTokenProviders` described in Caching section.