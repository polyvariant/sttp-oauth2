package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import com.ocadotechnology.sttp.oauth2.json.JsonDecoder
import com.ocadotechnology.sttp.oauth2.json.JsonDecoders
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

trait UserInfoSerializationSpec extends AnyFlatSpecLike with Matchers {

  this: JsonDecoders =>

  "UserInfo" should "deserialize incomplete user info" in {
    val subject = "jane.doe@ocado"
    val name = "Jane Doe"
    val givenName = "Jane"
    val familyName = "Doe"
    val domain = "ocado"
    val preferredName = "jane.doe"
    val email = "jane.doe@ocado.com"
    val emailVerified = true
    val locale = "en-GB"
    val site = "c279231e-e528-4f49-8a72-490b95fa1134"
    val banners = Nil
    val regions = Nil
    val fulfillmentContext = "97c08b89-8984-4672-a679-5cd090a605a3"
    val jsonToken =
      // language=JSON
      s"""
      {
        "sub": "$subject",
        "name": "$name",
        "given_name": "$givenName",
        "family_name": "$familyName",
        "domain": "$domain",
        "preferred_username": "$preferredName",
        "email": "$email",
        "email_verified": $emailVerified,
        "locale": "$locale",
        "sites": [ "$site" ],
        "fulfillment_contexts": [ "$fulfillmentContext" ]
      }
      """

    JsonDecoder[UserInfo].decodeString(jsonToken) shouldBe Right(
      UserInfo(
        subject.some,
        name.some,
        givenName.some,
        familyName.some,
        None,
        domain.some,
        preferredName.some,
        email.some,
        emailVerified.some,
        locale.some,
        List(site),
        banners,
        regions,
        List(fulfillmentContext)
      )
    )
  }

  "UserInfo" should "deserialize complete user info" in {
    val subject = "jane.doe@ocado"
    val name = "Jane Doe"
    val givenName = "Jane"
    val familyName = "Doe"
    val jobTitle = "Software Developer"
    val domain = "ocado"
    val preferredName = "jane.doe"
    val email = "jane.doe@ocado.com"
    val emailVerified = true
    val locale = "en-GB"
    val site = "c279231e-e528-4f49-8a72-490b95fa1134"
    val banner = "c76bcc03-e73d-40ae-ab16-8e2ad43ca6ef"
    val region = "b608c818-bdc8-4129-b76a-17bd5c66e9db"
    val fulfillmentContext = "97c08b89-8984-4672-a679-5cd090a605a3"
    val jsonToken =
      // language=JSON
      s"""
      {
        "sub": "$subject",
        "name": "$name",
        "given_name": "$givenName",
        "family_name": "$familyName",
        "job_title": "$jobTitle",
        "domain": "$domain",
        "preferred_username": "$preferredName",
        "email": "$email",
        "email_verified": $emailVerified,
        "locale": "$locale",
        "sites": [ "$site" ],
        "banners": [ "$banner" ],
        "regions": [ "$region" ],
        "fulfillment_contexts": [ "$fulfillmentContext" ]
      }
      """

    JsonDecoder[UserInfo].decodeString(jsonToken) shouldBe Right(
      UserInfo(
        subject.some,
        name.some,
        givenName.some,
        familyName.some,
        jobTitle.some,
        domain.some,
        preferredName.some,
        email.some,
        emailVerified.some,
        locale.some,
        List(site),
        List(banner),
        List(region),
        List(fulfillmentContext)
      )
    )
  }

}
