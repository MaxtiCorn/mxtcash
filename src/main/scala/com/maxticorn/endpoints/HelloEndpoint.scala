package com.maxticorn.endpoints

import cats.data.{Kleisli, OptionT}
import cats.instances.option._
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.{Defer, Monad}
import com.maxticorn.domain.User
import com.maxticorn.service.AppService
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware
import org.http4s.util.CaseInsensitiveString
import org.http4s.{AuthedRoutes, HttpApp, Request}

class HelloEndpoint[F[_]: Monad: Defer](dsl: Http4sDsl[F], appService: AppService[F]) {
  import dsl._

  private val authUser = Kleisli((req: Request[F]) =>
    OptionT {
      for {
        header <- req.headers.get(CaseInsensitiveString("authorization")).pure[F]
        user <- header.flatTraverse { h =>
          h.value.split(";") match {
            case Array(login, password) => appService.getUser(login, password)
            case _                      => Option.empty[User].pure[F]
          }
        }
      } yield user
    }
  )

  private val authedRoutes: AuthedRoutes[User, F] =
    AuthedRoutes.of {
      case GET -> Root / "hello" as user => Ok(s"Hello, ${user.login}")
    }

  val httpApp: HttpApp[F] = AuthMiddleware(authUser).apply(authedRoutes).orNotFound
}
