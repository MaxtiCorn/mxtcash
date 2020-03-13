package com.maxticorn.config

import zio.{ZIO, system}
import zio.system.System

object ConfigProvider {
  def serverConfig: ZIO[System, RuntimeException, ServerConfig] =
    for {
      host <- system
        .env("HOST")
        .someOrFailException[String, RuntimeException]

      port <- system
        .env("PORT")
        .map(_.flatMap(_.toIntOption))
        .someOrFailException[Int, RuntimeException]

    } yield ServerConfig(host, port)
}
