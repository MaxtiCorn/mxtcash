package com.maxticorn.endpoints

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import com.maxticorn.components.ServiceComponent
import com.maxticorn.domain.LoginRequest
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Response}
import tsec.authentication.{SecuredRequestHandler, TSecAuthService}
import tsec.authentication._

class Endpoints[F[_]: Sync](serviceComponent: ServiceComponent[F]) {
  val dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl._

  implicit val decoder: EntityDecoder[F, LoginRequest] = jsonOf[F, LoginRequest]

  val login: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" =>
      for {
        loginRequest <- req.as[LoginRequest]
        token        <- serviceComponent.authService.jwtToken(loginRequest.login, loginRequest.password)
        response     <- token.fold(Response.notFound[F].pure)(token => Ok(token.asJson))
      } yield response
  }

  val api: HttpRoutes[F] =
    SecuredRequestHandler(serviceComponent.authService.jwtAuthenticator).liftService(TSecAuthService {
      case GET -> Root / "hello" asAuthed user =>
        Ok(s"Hello, ${user.login}".asJson)
    })
}

object Endpoints {
  def apply[F[_]: Sync](serviceComponent: ServiceComponent[F]): Endpoints[F] =
    new Endpoints(serviceComponent)
}
