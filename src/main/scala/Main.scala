import cats.effect.ExitCode._
import endpoints.HelloEndpoint
import fs2.Stream
import org.http4s.dsl._
import org.http4s.implicits._
import zio.interop.catz._
import zio.{RIO, ZEnv, ZIO, system}

object Main extends zio.App {
  type AppTask[A] = RIO[ZEnv, A]

  def port(): ZIO[system.System, Throwable, Int] =
    (for {
      property <- system.property("http.port")
      port = property.flatMap(_.toIntOption)
    } yield port) someOrFail new RuntimeException(
      "couldn't read http.port property"
    )

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    ZIO.runtime[ZEnv].flatMap { implicit rt =>
      (for {
        port <- Stream.eval(port())
        helloEndpoint = new HelloEndpoint(Http4sDsl[AppTask])
        httpApp = helloEndpoint.routes.orNotFound
        server = new HttpServerImpl("0.0.0.0", port, httpApp)
        _ <- server.serve
      } yield ()).compile.drain
        .foldM(
          th => ZIO.die(th).as(Error.code),
          _ => ZIO.succeed(Success.code)
        )
    }
  }
}
