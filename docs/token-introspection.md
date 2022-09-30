---
sidebar_position: 5
description: Token introspection
---

Token introspection interface provides one method, that helps you ask the OAuth2 provider details about the token. The response is described in [rfc7662 section 2.2](https://datatracker.ietf.org/doc/html/rfc7662#section-2.2). The only guaranteed field is `active` that determines if the token is still valid.

```scala
trait TokenIntrospection[F[_]] {

  /** Introspects passed token in OAuth2 provider.
    *
    * Successful introspections returns `F[TokenIntrospectionResponse.IntrospectionResponse]`.
    */
  def introspect(token: Secret[String]): F[Introspection.TokenIntrospectionResponse]

}
```

The `oauth2-cache-cats` module provides the cached version of this interface `CachingTokenIntrospection` that allows you to limit the calls to the OAuth2 provider. To use it you need to provide regular `TokenIntrospection`, the cache implementation and the default expiration time, since the introspection response doesn't necessarily provide such information.

The cache implementation can be anything that implements `ExpiringCache` trait, for the out of the box solution use `CatsRefExpiringCache`.
