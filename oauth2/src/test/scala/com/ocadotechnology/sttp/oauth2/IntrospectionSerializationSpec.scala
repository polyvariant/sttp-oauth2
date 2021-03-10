package com.ocadotechnology.sttp.oauth2

import com.ocadotechnology.sttp.oauth2.Introspection.TokenIntrospectionResponse
import com.ocadotechnology.sttp.oauth2.common.Scope
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.circe.literal._
import org.scalatest.OptionValues

import java.time.Instant

class IntrospectionSerializationSpec extends AnyWordSpec with Matchers with OptionValues {
  "Token" should {
    "deserialize token introspection response" in {
      val clientId = "Client ID"
      val domain = "zoo"
      val exp = Instant.EPOCH
      val active = false
      val authorities = List("aaa", "bbb")
      val scope = "cfc.first-app_scope"
      val tokenType = "Bearer"

      val json = json"""{
            "client_id": $clientId,
            "domain": $domain,
            "exp": ${exp.getEpochSecond},
            "active": $active,
            "authorities": $authorities,
            "scope": $scope,
            "token_type": $tokenType
          }"""

      json.as[TokenIntrospectionResponse] shouldBe Right(
        TokenIntrospectionResponse(clientId, domain, exp, active, authorities, Scope.of(scope).value, tokenType)
      )

    }
  }
}
