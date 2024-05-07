[release]:       https://github.com/polyvariant/sttp-oauth2/releases/latest
[release-badge]: https://img.shields.io/github/release/polyvariant/sttp-oauth2.svg

# sttp-oauth2 - OAuth2 client library for Scala

This library aims to provide easy integration with OAuth2 providers based on [OAuth2 RFC](https://tools.ietf.org/html/rfc6749) using [sttp](https://github.com/softwaremill/sttp) client. It uses [circe](https://github.com/circe/circe) for JSON serialization/deserialization.

Currently it supports methods (grant types) for obtaining authorization:
 - [Authorization code](https://tools.ietf.org/html/rfc6749#section-4.1)
 - [Password grant](https://tools.ietf.org/html/rfc6749#section-4.3)
 - [Client credentials](https://tools.ietf.org/html/rfc6749#section-4.4)

## Quick start

To use this library add the following dependency to your `build.sbt`

Versions 0.19.0 and newer

```scala
"org.polyvariant" %% "sttp-oauth2" % "x.y.z"
```

Versions up to 0.18.0

```scala
"com.ocadotechnology" %% "sttp-oauth2" % "x.y.z"
```

The latest release is: [![release-badge][]][release]

## Documentation

Visit the documentation at [https://polyvariant.github.io/sttp-oauth2](https://polyvariant.github.io/sttp-oauth2/) for usage instructions.

## Contributing

Feel free to submit feature requests and bug reports under Issues.

## License

sttp-oauth2 is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) (the "License"); you may not use this software except in compliance with the License.

## Honorable mentions

sttp-oauth2 was initially created by [Ocado Technology](https://github.com/ocadotechnology) 
