package com.maxticorn.domain

import io.circe.Decoder
import io.circe.generic.semiauto._

case class Credentials(login: String, password: String)

object Credentials {
  implicit val loginRequestDecoder: Decoder[Credentials] = deriveDecoder[Credentials]
}
