package org.polyvariant.sttp.oauth2.json

import org.polyvariant.sttp.oauth2.ClientCredentialsTokenDeserializationSpec
import org.polyvariant.sttp.oauth2.IntrospectionSerializationSpec
import org.polyvariant.sttp.oauth2.OAuth2ErrorDeserializationSpec
import org.polyvariant.sttp.oauth2.TokenSerializationSpec
import org.polyvariant.sttp.oauth2.UserInfoSerializationSpec

abstract class JsonSpec
  extends ClientCredentialsAccessTokenResponseDeserializationSpec
  with ClientCredentialsTokenDeserializationSpec
  with IntrospectionSerializationSpec
  with UserInfoSerializationSpec
  with TokenSerializationSpec
  with OAuth2ErrorDeserializationSpec
  with JsonDecoders
