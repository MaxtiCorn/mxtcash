package com.maxticorn.components

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import cats.Monad
import com.maxticorn.service.AppService
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{HttpApp, HttpRoutes, Response}
import tsec.authentication.{SecuredRequestHandler, TSecAuthService}
import tsec.authentication._

class HttpComponent[F[_]: Monad: Sync](dsl: Http4sDsl[F], appService: AppService[F]) {
  import dsl._

  private val securedRequestHandler = SecuredRequestHandler(appService.jwtAuthenticator)

  private object loginMatcher    extends QueryParamDecoderMatcher[String]("login")
  private object passwordMatcher extends QueryParamDecoderMatcher[String]("password")

  val httpApp: HttpApp[F] = {

    val login = HttpRoutes.of[F] {
      case GET -> Root / "login" :? loginMatcher(login) +& passwordMatcher(password) =>
        for {
          token    <- appService.jwtToken(login, password)
          response <- token.fold(Response.notFound[F].pure)(Ok(_))
        } yield response
    }

    val api = securedRequestHandler.liftService(TSecAuthService {
      case GET -> Root / "hello" asAuthed user => Ok(s"Hello, ${user.login}")
    })

    Router(
      ""    -> login,
      "api" -> api
    ).orNotFound

  }
}
