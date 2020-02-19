import cats.effect.{ExitCode, Resource}
import monix.eval.{Task, TaskApp}
import org.http4s.HttpRoutes
import org.http4s.dsl._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import scala.util.Try

object Main extends TaskApp {
  override def run(args: List[String]): Task[ExitCode] = {
    val dsl = Http4sDsl[Task]
    import dsl._

    val routes = HttpRoutes.of[Task] {
      case GET -> Root / "hello" / name =>
        Ok(s"hello, $name")
    }

    (for {
      port <- Resource.liftF(
        Task.fromTry(Try(System.getProperty("http.port").toInt))
      )
      _ <- BlazeServerBuilder[Task]
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(routes.orNotFound)
        .resource
    } yield ()).use { _ =>
      Task.never
    }
  }
}
