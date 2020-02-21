package config

import zio.{ZIO, system}

object ConfigProvider {
  def serverConfig: ZIO[system.System, Throwable, ServerConfig] =
    for {
      host <- system
        .env("HOST")
        .someOrFail(new RuntimeException(s"couldn't read host"))

      port <- system
        .env("PORT")
        .map(_.flatMap(_.toIntOption))
        .someOrFail(new RuntimeException(s"couldn't read port"))

    } yield ServerConfig(host, port)
}
