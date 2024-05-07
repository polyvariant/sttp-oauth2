---
sidebar_position: 1
description: Getting started with sttp-oauth2 
---

# Getting started

## About

This library aims to provide easy integration with OAuth2 providers based on [OAuth2 RFC](https://tools.ietf.org/html/rfc6749) using [sttp](https://github.com/softwaremill/sttp) client.
There are multiple JSON implementations, see [JSON deserialisation](json-deserialisation.md) for details.

## Installation

To use this library add following dependency to your `build.sbt` file
```scala
"org.polyvariant" %% "sttp-oauth2" % "@VERSION@"
"org.polyvariant" %% "sttp-oauth2-circe" % "@VERSION@" // Or other, see JSON support
```
## Usage

Depending on your use case, please see documentation for the grant you want to support.

Each grant is implemented in an object with explicit return and error types on methods and additionally, Tagless Final friendly `*Provider` interface.

All grant implementations require a set of implicit `JsonDecoder`s, e.g.:
```scala
import org.polyvariant.sttp.oauth2.json.circe.instances._
```

See [JSON deserialisation](json-deserialisation.md) for details.
