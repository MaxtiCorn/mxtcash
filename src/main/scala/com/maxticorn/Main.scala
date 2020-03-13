package com.maxticorn

import java.util.concurrent.Executors._

import endpoints.Endpoints
import db.Db
import config.ConfigProvider
import cats.effect.{Blocker, Resource}
import doobie.h2.H2Transactor
import components.{HttpComponent, ServiceComponent}
import zio.{Has, RIO, Runtime, ZIO}
import zio.clock.Clock
import zio.system.System
import zio.blocking.Blocking
import zio.internal.Platform
import zio.interop.catz._

import scala.concurrent.ExecutionContext.fromExecutor
import scala.concurrent.ExecutionContext

object Main {
  type Env = Clock with System with Blocking
  val executor: ExecutionContext = fromExecutor(newWorkStealingPool())
  implicit val runtime: Runtime[Env] =
    RuntimeUtils(
      Has(Clock.Service.live) ++ Has(System.Service.live) ++ Has(Blocking.Service.live),
      executor
    )

  private def dbResource: Resource[RIO[Env, *], Db[RIO[Env, *]]] =
    for {
      blockingExecutor <- Resource.liftF(ZIO.access[Blocking](_.get.blockingExecutor))
      transactor <- H2Transactor.newH2Transactor(
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        "username",
        "password",
        executor,
        Blocker.liftExecutionContext(blockingExecutor.asEC)
      )
    } yield Db(transactor)

  def program: ZIO[Env, Throwable, Nothing] =
    (for {
      db               <- dbResource.toManaged
      serviceComponent <- ServiceComponent.mk(db).toManaged_
      httpComponent    = HttpComponent(Endpoints(serviceComponent))
      serverConfig     <- ConfigProvider.serverConfig.toManaged_
      server           = HttpServer(serverConfig, httpComponent, executor)
      _                <- server.builder.resource.toManaged
    } yield ()).useForever

  final def main(args: Array[String]): Unit =
    runtime.unsafeRun(
      for {
        fiber <- program.fork
        _     <- ZIO.effectTotal(Platform.addShutdownHook(() => runtime.unsafeRun(fiber.interrupt)))
        _     <- fiber.join
        _     <- fiber.interrupt
      } yield ()
    )
}
