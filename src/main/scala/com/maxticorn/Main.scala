package com.maxticorn

import cats.effect.{Blocker, Resource}
import com.maxticorn.db.Db
import com.maxticorn.service.AppService
import config.ConfigProvider
import doobie.h2.H2Transactor
import endpoints.HelloEndpoint
import org.http4s.dsl._
import zio.blocking.Blocking
import zio.system.System
import zio.clock.Clock
import zio.interop.catz._
import zio.{RIO, ZIO}

object Main extends zio.App {
  type AppEnv     = Clock with System with Blocking
  type AppTask[A] = RIO[AppEnv, A]

  private def transactorResource(implicit runtime: zio.Runtime[Blocking]): Resource[AppTask, H2Transactor[AppTask]] =
    for {
      blockingExecutor <- Resource.liftF(runtime.environment.blocking.blockingExecutor)
      blocker          = Blocker.liftExecutionContext(blockingExecutor.asEC)
      transactor <- H2Transactor.newH2Transactor[AppTask](
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        "username",
        "password",
        runtime.platform.executor.asEC,
        blocker
      )
    } yield transactor

  override def run(args: List[String]): ZIO[AppEnv, Nothing, Int] =
    (for {
      implicit0(runtime: zio.Runtime[AppEnv]) <- ZIO.runtime[AppEnv].toManaged_
      transactor                              <- transactorResource.toManaged
      appService                              = new AppService(new Db[AppTask], transactor)
      _                                       <- appService.init.toManaged_
      serverConfig                            <- ConfigProvider.serverConfig.toManaged_
      helloEndpoint                           = new HelloEndpoint(Http4sDsl[AppTask], appService)
      httpApp                                 = helloEndpoint.httpApp
      server                                  = new HttpServerImpl(serverConfig, httpApp)
      _                                       <- server.builder.resource.toManaged
    } yield ()).useForever
      .foldM(
        _ => ZIO.succeed(1),
        _ => ZIO.succeed(0)
      )
}
