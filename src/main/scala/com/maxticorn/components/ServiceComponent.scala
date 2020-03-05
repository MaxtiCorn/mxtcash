package com.maxticorn.components

import cats.syntax.functor._
import cats.effect.Sync
import com.maxticorn.db.Db
import com.maxticorn.service.AuthService

class ServiceComponent[F[_]](val authService: AuthService[F])

object ServiceComponent {
  def mk[F[_]: Sync](db: Db[F]): F[ServiceComponent[F]] =
    for {
      authService <- AuthService.mk(db)
    } yield new ServiceComponent[F](authService)
}
