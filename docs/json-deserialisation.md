---
sidebar_position: 7
description: Choosing JSON deserialisation module
---

# Choosing JSON deserialisation module
JSON deserialisation has been decoupled from the core modules.
There are now a couple of options to choose from:

## circe
To use [circe](https://github.com/circe/circe) implementation
add the following module to your dependencies:

```scala
"com.ocadotechnology" %% "sttp-oauth2-circe" % "@VERSION@"
```

Then import appropriate set of implicit instances:

```scala
import com.ocadotechnology.sttp.oauth2.json.circe.instances._
```

## jsoniter-scala
To use [jsoniter-scala](https://github.com/plokhotnyuk/jsoniter-scala) implementation
add the following module to your dependencies:

```scala
"com.ocadotechnology" %% "sttp-oauth2-jsoniter" % "@VERSION@"
```

Then import appropriate set of implicit instances:

```scala
import com.ocadotechnology.sttp.oauth2.json.jsoniter.instances._
```