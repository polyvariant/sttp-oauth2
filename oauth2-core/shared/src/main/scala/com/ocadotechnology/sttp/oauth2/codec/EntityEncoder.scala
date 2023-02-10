package com.ocadotechnology.sttp.oauth2.codec

trait EntityEncoder[A] {
  def encode: EncodedData
}
