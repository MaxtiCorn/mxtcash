import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import fs2.Stream
import org.http4s.HttpApp
import org.http4s.server.blaze.BlazeServerBuilder

trait HttpServer[F[_]] {
  def serve: Stream[F, ExitCode]
}

class HttpServerImpl[F[_]: ConcurrentEffect: Timer](
    host: String,
    port: Int,
    httpApp: HttpApp[F]
) extends HttpServer[F] {
  def serve: Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(port, host)
      .withHttpApp(httpApp)
      .serve
}
