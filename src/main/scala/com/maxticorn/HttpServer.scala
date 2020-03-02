package com.maxticorn

import cats.effect.{ConcurrentEffect, Timer}
import com.maxticorn.config.ServerConfig
import org.http4s.HttpApp
import org.http4s.server.blaze.BlazeServerBuilder

trait HttpServer[F[_]] {
  def builder: BlazeServerBuilder[F]
}

class HttpServerImpl[F[_]: ConcurrentEffect: Timer](config: ServerConfig, httpApp: HttpApp[F])
  extends HttpServer[F] {
  def builder: BlazeServerBuilder[F] =
    BlazeServerBuilder[F]
      .bindHttp(config.port, config.host)
      .withHttpApp(httpApp)
}
