package com.maxticorn

import java.util.concurrent.Executors._

import endpoints.Endpoints
import db.Db
import config.ConfigProvider
import cats.effect.{Blocker, Resource}
import doobie.h2.H2Transactor
import components.{HttpComponent, ServiceComponent}
import zio.interop.catz._
import zio.blocking.Blocking
import zio.{RIO, ZEnv, ZIO}

import scala.concurrent.ExecutionContext.fromExecutor

object Main extends CatsApp {

  private def dbResource: Resource[RIO[ZEnv, *], Db[RIO[ZEnv, *]]] =
    for {
      blockingExecutor <- Resource.liftF(ZIO.access[Blocking](_.get.blockingExecutor))
      transactor <- H2Transactor.newH2Transactor(
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        "username",
        "password",
        fromExecutor(newWorkStealingPool()),
        Blocker.liftExecutionContext(blockingExecutor.asEC)
      )
    } yield Db(transactor)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      db               <- dbResource.toManaged
      serviceComponent <- ServiceComponent.mk(db).toManaged_
      httpComponent    = HttpComponent(Endpoints(serviceComponent))
      serverConfig     <- ConfigProvider.serverConfig.toManaged_
      server           = HttpServer(serverConfig, httpComponent, fromExecutor(newWorkStealingPool()))
      _                <- server.builder.resource.toManaged
    } yield ()).useForever
      .fold(_ => 1, _ => 0)
}
