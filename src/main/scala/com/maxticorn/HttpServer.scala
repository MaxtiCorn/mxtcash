package com.maxticorn

import cats.effect.{ConcurrentEffect, Timer}
import com.maxticorn.components.HttpComponent
import com.maxticorn.config.ServerConfig
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

trait HttpServer[F[_]] {
  def builder: BlazeServerBuilder[F]
}

class HttpServerImpl[F[_]: ConcurrentEffect: Timer](
  config: ServerConfig,
  httpComponent: HttpComponent[F],
  executionContext: ExecutionContext
) extends HttpServer[F] {
  def builder: BlazeServerBuilder[F] =
    BlazeServerBuilder[F]
      .withExecutionContext(executionContext)
      .withNio2(true)
      .bindHttp(config.port, config.host)
      .withHttpApp(httpComponent.httpApp)
      .withoutBanner
}

object HttpServer {
  def apply[F[_]: ConcurrentEffect: Timer](
    config: ServerConfig,
    httpComponent: HttpComponent[F],
    executionContext: ExecutionContext
  ): HttpServer[F] =
    new HttpServerImpl(config, httpComponent, executionContext)
}
