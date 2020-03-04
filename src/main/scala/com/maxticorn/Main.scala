package com.maxticorn

import cats.effect.{Blocker, Resource}
import com.maxticorn.endpoints.Endpoints
import com.maxticorn.db.Db
import com.maxticorn.service.AuthService
import config.ConfigProvider
import doobie.h2.H2Transactor
import components.{HttpComponent, ServiceComponent}
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
      db                                      = Db(transactor)
      authService                             <- AuthService.mk(db).toManaged_
      serviceComponent                        <- ServiceComponent.mk(authService).toManaged_
      endpoints                               = Endpoints(serviceComponent)
      httpComponent                           = HttpComponent(endpoints)
      serverConfig                            <- ConfigProvider.serverConfig.toManaged_
      server                                  = HttpServer(serverConfig, httpComponent)
      _                                       <- server.builder.resource.toManaged
    } yield ()).useForever
      .foldM(
        _ => ZIO.succeed(1),
        _ => ZIO.succeed(0)
      )
}
