package endpoints

import cats.{Applicative, Defer}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class HelloEndpoint[F[_]: Applicative: Defer](dsl: Http4sDsl[F]) {
  import dsl._

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name")
  }
}
