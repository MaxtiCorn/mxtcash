package com.maxticorn

import config.ConfigProvider
import endpoints.HelloEndpoint
import org.http4s.dsl._
import org.http4s.implicits._
import zio.system.System
import zio.clock.Clock
import zio.interop.catz._
import zio.{RIO, ZIO}

object Main extends zio.App {
  type AppEnv     = Clock with System
  type AppTask[A] = RIO[AppEnv, A]

  override def run(args: List[String]): ZIO[AppEnv, Nothing, Int] =
    (for {
      implicit0(runtime: zio.Runtime[AppEnv]) <- ZIO.runtime[AppEnv].toManaged_
      serverConfig                            <- ConfigProvider.serverConfig.toManaged_
      helloEndpoint                           = new HelloEndpoint(Http4sDsl[AppTask])
      httpApp                                 = helloEndpoint.routes.orNotFound
      server                                  = new HttpServerImpl(serverConfig, httpApp)
      _                                       <- server.builder.resource.toManaged
    } yield ()).useForever
      .foldM(
        _ => ZIO.succeed(1),
        _ => ZIO.succeed(0)
      )
}
