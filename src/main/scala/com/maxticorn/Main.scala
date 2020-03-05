package com.maxticorn

import java.util.concurrent.Executors._

import endpoints.Endpoints
import db.Db
import config.ConfigProvider
import cats.effect.{Blocker, Resource}
import doobie.h2.H2Transactor
import components.{HttpComponent, ServiceComponent}
import zio.blocking.Blocking
import zio.internal.PlatformLive.defaultYieldOpCount
import zio.internal.Executor.fromExecutionContext
import zio.interop.catz._
import zio.{RIO, ZEnv, ZIO}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.ExecutionContext.fromExecutor

object Main extends zio.App {
  type AppTask[A] = RIO[ZEnv, A]

  val executor: ExecutionContextExecutor = fromExecutor(newWorkStealingPool())
  implicit val runtime: zio.Runtime[ZEnv] = this
    .withExecutor(fromExecutionContext(defaultYieldOpCount)(executor))

  private def dbResource: Resource[AppTask, Db[AppTask]] =
    for {
      blockingExecutor <- Resource.liftF(ZIO.accessM[Blocking](_.blocking.blockingExecutor))
      transactor <- H2Transactor.newH2Transactor[AppTask](
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        "username",
        "password",
        executor,
        Blocker.liftExecutionContext(blockingExecutor.asEC)
      )
    } yield Db(transactor)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      db               <- dbResource.toManaged
      serviceComponent <- ServiceComponent.mk(db).toManaged_
      httpComponent    = HttpComponent(Endpoints(serviceComponent))
      serverConfig     <- ConfigProvider.serverConfig.toManaged_
      server           = HttpServer(serverConfig, httpComponent, executor)
      _                <- server.builder.resource.toManaged
    } yield ()).useForever
      .fold(_ => 1, _ => 0)
}
