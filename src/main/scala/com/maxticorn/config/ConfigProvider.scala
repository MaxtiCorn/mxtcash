package com.maxticorn.config

import zio.{ZIO, system}

object ConfigProvider {
  def serverConfig(implicit runtime: zio.Runtime[system.System]): ZIO[system.System, Throwable, ServerConfig] =
    for {
      host <- runtime.environment.system
        .env("HOST")
        .someOrFail(new RuntimeException(s"couldn't read host"))

      port <- runtime.environment.system
        .env("PORT")
        .map(_.flatMap(_.toIntOption))
        .someOrFail(new RuntimeException(s"couldn't read port"))

    } yield ServerConfig(host, port)
}
