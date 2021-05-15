---
sidebar_position: 1
---

# Getting started

## About

This library aims to provide easy integration with OAuth2 providers based on [OAuth2 RFC](https://tools.ietf.org/html/rfc6749) using [sttp](https://github.com/softwaremill/sttp) client.
It uses [circe](https://github.com/circe/circe) for JSON serialization/deserialization.

## Installation

To use this library add following dependency to your `build.sbt` file
```scala
"com.ocadotechnology" %% "sttp-oauth2" % "@VERSION@"
```
## Usage

Depending on your use case, please see documentation for the grant you want to support.

Each grant is implemented in an object with explicit return and error types on methods and additionally, TaglessFinal friendly `*Provider` interface.
