package com.maxticorn.service

import cats.effect.Bracket
import com.maxticorn.db.Db
import com.maxticorn.domain.User
import doobie.util.transactor.Transactor
import doobie.syntax.connectionio._

class AppService[F[_]](db: Db[F], transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable]) {
  def init: F[Unit] = db.createTables.transact(transactor)

  def getUser(login: String, password: String): F[Option[User]] =
    db.getUser(login, password).transact(transactor)
}
