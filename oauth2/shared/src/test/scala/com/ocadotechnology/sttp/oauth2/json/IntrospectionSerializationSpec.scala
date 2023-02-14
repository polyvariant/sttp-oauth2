package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.common.Scope
import com.ocadotechnology.sttp.oauth2.json.JsonDecoders
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import com.ocadotechnology.sttp.oauth2.codec.EntityDecoder
import org.scalatest.flatspec.AnyFlatSpecLike

import java.time.Instant

trait IntrospectionSerializationSpec extends AnyFlatSpecLike with Matchers with OptionValues {
  this: JsonDecoders =>

  "Token" should "deserialize token introspection response" in {
    val clientId = "Client ID"
    val domain = "mock"
    val exp = Instant.EPOCH
    val active = false
    val authority1 = "aaa"
    val authority2 = "bbb"
    val authorities = List(authority1, authority2)
    val scope = "cfc.first-app_scope"
    val tokenType = "Bearer"
    val audience = "Aud1"

    val json =
      s"""{
            "client_id": "$clientId",
            "domain": "$domain",
            "exp": ${exp.getEpochSecond},
            "active": $active,
            "authorities": [ "$authority1", "$authority2" ],
            "scope": "$scope",
            "token_type": "$tokenType",
            "aud": "$audience"
          }"""

    EntityDecoder[TokenIntrospectionResponse].decodeString(json) shouldBe Right(
      TokenIntrospectionResponse(
        active = active,
        clientId = Some(clientId),
        domain = Some(domain),
        exp = Some(exp),
        authorities = Some(authorities),
        scope = Some(Scope.of(scope).value),
        tokenType = Some(tokenType),
        aud = Some(Introspection.StringAudience(audience))
      )
    )

  }
}
