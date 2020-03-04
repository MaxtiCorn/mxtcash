package com.maxticorn.domain

import io.circe.Decoder
import io.circe.generic.semiauto._

case class LoginRequest(login: String, password: String)

object LoginRequest {
  implicit val loginRequestDecoder: Decoder[LoginRequest] = deriveDecoder[LoginRequest]
}
