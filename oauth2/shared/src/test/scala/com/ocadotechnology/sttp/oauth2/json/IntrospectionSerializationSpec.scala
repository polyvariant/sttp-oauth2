package org.polyvariant.sttp.oauth2

import org.polyvariant.sttp.oauth2.Introspection.TokenIntrospectionResponse
import org.polyvariant.sttp.oauth2.common.Scope
import org.polyvariant.sttp.oauth2.json.JsonDecoder
import org.polyvariant.sttp.oauth2.json.JsonDecoders
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpecLike

import java.time.Instant

trait IntrospectionSerializationSpec extends AnyFlatSpecLike with Matchers with OptionValues {
  this: JsonDecoders =>

  val clientId = "Client ID"
  val domain = "mock"
  val exp = Instant.EPOCH
  val active = false
  val authority1 = "aaa"
  val authority2 = "bbb"
  val authorities = List(authority1, authority2)
  val scope = "cfc.first-app_scope"
  val tokenType = "Bearer"

  "Token" should "deserialize token introspection response with a string audience" in {
    val audience = "Aud1"

    val json =
      // language=JSON
      s"""
      {
        "client_id": "$clientId",
        "domain": "$domain",
        "exp": ${exp.getEpochSecond},
        "active": $active,
        "authorities": [ "$authority1", "$authority2" ],
        "scope": "$scope",
        "token_type": "$tokenType",
        "aud": "$audience"
      }
      """

    JsonDecoder[TokenIntrospectionResponse].decodeString(json) shouldBe Right(
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

  "Token" should "deserialize token introspection response with a array of audiences" in {
    val audience = """["Aud1", "Aud2"]"""

    val json =
      // language=JSON
      s"""
      {
        "client_id": "$clientId",
        "domain": "$domain",
        "exp": ${exp.getEpochSecond},
        "active": $active,
        "authorities": [ "$authority1", "$authority2" ],
        "scope": "$scope",
        "token_type": "$tokenType",
        "aud": $audience
      }
      """

    JsonDecoder[TokenIntrospectionResponse].decodeString(json) shouldBe Right(
      TokenIntrospectionResponse(
        active = active,
        clientId = Some(clientId),
        domain = Some(domain),
        exp = Some(exp),
        authorities = Some(authorities),
        scope = Some(Scope.of(scope).value),
        tokenType = Some(tokenType),
        aud = Some(Introspection.SeqAudience(Seq("Aud1", "Aud2")))
      )
    )
  }
}
