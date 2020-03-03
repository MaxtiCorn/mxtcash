package com.maxticorn.service

import com.maxticorn.db.Db
import doobie.util.transactor.Transactor
import doobie.syntax.connectionio._
import tsec.authentication.TSecJWTSettings
import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.instances.option._
import cats.Monad
import com.maxticorn.domain.User
import tsec.authentication.JWTAuthenticator
import tsec.mac.jca.{HMACSHA256, MacSigningKey}

import scala.concurrent.duration._

class AppService[F[_]: Monad: Sync](db: Db[F], transactor: Transactor[F], key: MacSigningKey[HMACSHA256]) {

  def init: F[Unit] = db.createTables.transact(transactor)

  val jwtAuthenticator: JWTAuthenticator[F, String, User, HMACSHA256] =
    JWTAuthenticator.unbacked.inHeader(
      TSecJWTSettings(expiryDuration = 10.minutes, maxIdle = None),
      (id: String) => OptionT(db.getUser(id).transact(transactor)),
      key
    )

  def jwtToken(login: String, password: String): F[Option[String]] =
    for {
      userOpt  <- getUser(login, password)
      response <- userOpt.traverse(user => jwtAuthenticator.create(user.id).map(_.jwt.toEncodedString))
    } yield response

  def getUser(login: String, password: String): F[Option[User]] =
    db.getUser(login, password).transact(transactor)
}
