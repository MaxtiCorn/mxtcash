package com.maxticorn.components

import cats.Functor
import cats.syntax.functor._
import com.maxticorn.service.AuthService

class ServiceComponent[F[_]](val authService: AuthService[F])

object ServiceComponent {
  def mk[F[_]: Functor](authService: AuthService[F]): F[ServiceComponent[F]] =
    for {
      _ <- authService.init
    } yield new ServiceComponent[F](authService)
}
