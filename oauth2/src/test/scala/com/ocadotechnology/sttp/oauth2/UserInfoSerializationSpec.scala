package com.ocadotechnology.sttp.oauth2

import cats.syntax.all._
import io.circe.literal._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UserInfoSerializationSpec extends AnyWordSpec with Matchers {

  "UserInfo" should {
    "deserialize user info" in {
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
      val sites = List("c279231e-e528-4f49-8a72-490b95fa1134")
      val banners = List("c76bcc03-e73d-40ae-ab16-8e2ad43ca6ef")
      val regions = List("b608c818-bdc8-4129-b76a-17bd5c66e9db")
      val fulfillmentContexts = List("97c08b89-8984-4672-a679-5cd090a605a3")
      val jsonToken =
        json"""{
                  "sub": $subject,
                  "name": $name,
                  "given_name": $givenName,
                  "family_name": $familyName,
                  "job_title": $jobTitle,
                  "domain": $domain,
                  "preferred_username": $preferredName,
                  "email": $email,
                  "email_verified": $emailVerified,
                  "locale": $locale,
                  "sites": $sites,
                  "banners": $banners,
                  "regions": $regions,
                  "fulfillment_contexts": $fulfillmentContexts
                }"""

      jsonToken.as[UserInfo] shouldBe Right(
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
          sites,
          banners,
          regions,
          fulfillmentContexts
        )
      )
    }
  }

}
