import cats.effect.ExitCode
import monix.eval.{Task, TaskApp}
import org.http4s.HttpRoutes
import org.http4s.dsl._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends TaskApp {
  override def run(args: List[String]): Task[ExitCode] = {
    val dsl = Http4sDsl[Task]
    import dsl._

    val routes = HttpRoutes.of[Task] {
      case GET -> Root / "hello" / name =>
        Ok(s"hello, $name")
    }

    BlazeServerBuilder[Task]
      .bindHttp(8080, "localhost")
      .withHttpApp(routes.orNotFound)
      .resource
      .use { _ =>
        Task.never
      }
  }
}
