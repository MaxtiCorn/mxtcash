import cats.{Applicative, Defer}
import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import cats.effect.ExitCode._
import zio.{RIO, ZIO, ZEnv, system}
import zio.interop.catz._
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.dsl._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends zio.App {
  type AppTask[A] = RIO[ZEnv, A]

  def routes[F[_]: Applicative: Defer](dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        Ok(s"hello, $name")
    }
  }

  def httpServe[F[_]: ConcurrentEffect: Timer](
      host: String,
      port: Int,
      routes: HttpRoutes[F]
  ): Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(port, host)
      .withHttpApp(routes.orNotFound)
      .serve

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
        _ <- httpServe("0.0.0.0", port, routes(Http4sDsl[AppTask]))
      } yield ()).compile.drain
        .foldM(
          th => ZIO.die(th).as(Error.code),
          _ => ZIO.succeed(Success.code)
        )
    }
  }
}
