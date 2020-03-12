package com.maxticorn.config

import zio.ZIO
import zio.system.System

object ConfigProvider {
  def serverConfig: ZIO[System, Throwable, ServerConfig] =
    for {
      host <- ZIO.accessM[System](_.get
        .env("HOST")
        .someOrFail(new RuntimeException(s"couldn't read host")))

      port <- ZIO.accessM[System](_.get
        .env("PORT")
        .map(_.flatMap(_.toIntOption))
        .someOrFail(new RuntimeException(s"couldn't read port")))

    } yield ServerConfig(host, port)
}
