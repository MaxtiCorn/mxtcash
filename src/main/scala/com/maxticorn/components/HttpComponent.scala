package com.maxticorn.components

import cats.effect.Sync
import com.maxticorn.endpoints.Endpoints
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.Router

class HttpComponent[F[_]: Sync](endpoints: Endpoints[F]) {
  val httpApp: HttpApp[F] = {
    Router(
      ""    -> endpoints.login,
      "api" -> endpoints.api
    ).orNotFound
  }
}

object HttpComponent {
  def apply[F[_]: Sync](controller: Endpoints[F]): HttpComponent[F] =
    new HttpComponent(controller)
}
