package com.maxticorn.domain

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.semiauto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class Credentials(login: String, password: String)

object Credentials {
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, Credentials] = jsonOf[F, Credentials]
  implicit val loginRequestDecoder: Decoder[Credentials] = deriveDecoder[Credentials]
}
