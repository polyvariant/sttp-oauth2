---
sidebar_position: 6
description: Caching
---

# Caching

The sttp-oauth2 library comes with `CachingAccessTokenProvider` and `CachingTokenIntrospection` - interfaces that allow caching the responses provided by the OAuth2 provider. Both of those require an implementation of the `ExpiringCache` algebra, defined as follows: 

```scala
trait ExpiringCache[F[_], K, V] {
  def get(key: K): F[Option[V]]

  def put(key: K, value: V, expirationTime: Instant): F[Unit]

  def remove(key: K): F[Unit]
}
```

As the user of the library you can either choose to implement your own cache mechanism, or go for one of the provided:

| Class                     |Description                                                  | Import module     |
|---------------------------|-------------------------------------------------------------|-------------------|
| `CatsRefExpiringCache`    | Simple Cats Effect 3 Ref based implementation               | `"com.ocadotechnology" %% "sttp-oauth2-cache-cats" % "@VERSION@"` |
| `CatsRefExpiringCache`    | Simple Cats Effect 2 Ref based implementation               | `"com.ocadotechnology" %% "sttp-oauth2-cache-ce2" % "@VERSION@"` |
| `ScalacacheExpiringCache` | Implementation based on https://github.com/cb372/scalacache | `"com.ocadotechnology" %% "sttp-oauth2-cache-scalacache" % "@VERSION@"` |
| `MonixFutureCache`        | Future based implementation powered by [Monix](https://monix.io/) | `"com.ocadotechnology" %% "sttp-oauth2-cache-future" % "@VERSION@"` |

