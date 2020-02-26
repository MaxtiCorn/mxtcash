import cats.effect.ExitCode._
import config.ConfigProvider
import endpoints.HelloEndpoint
import fs2.Stream
import org.http4s.dsl._
import org.http4s.implicits._
import zio.interop.catz._
import zio.{RIO, ZEnv, ZIO}

object Main extends zio.App {
  type AppTask[A] = RIO[ZEnv, A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ZIO.runtime[ZEnv].flatMap { implicit rt =>
      (for {
        config        <- Stream.eval(ConfigProvider.serverConfig)
        helloEndpoint = new HelloEndpoint(Http4sDsl[AppTask])
        httpApp       = helloEndpoint.routes.orNotFound
        server        = new HttpServerImpl(config.host, config.port, httpApp)
        _             <- server.serve
      } yield ()).compile.drain
        .foldM(th => ZIO.die(th).as(Error.code), _ => ZIO.succeed(Success.code))
    }
}
