package com.maxticorn.service

import cats.data.OptionT
import cats.effect.Sync
import cats.instances.option._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import tsec.authentication.{JWTAuthenticator, TSecJWTSettings}
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
import com.maxticorn.db.Db
import com.maxticorn.domain.User

import scala.concurrent.duration._

trait AuthService[F[_]] {
  def init: F[Unit]
  def jwtAuthenticator: JWTAuthenticator[F, String, User, HMACSHA256]
  def jwtToken(login: String, password: String): F[Option[String]]
}

class AuthServiceImpl[F[_]: Sync](db: Db[F], key: MacSigningKey[HMACSHA256]) extends AuthService[F] {
  def init: F[Unit] = db.createUserTable

  val jwtAuthenticator: JWTAuthenticator[F, String, User, HMACSHA256] =
    JWTAuthenticator.unbacked.inHeader(
      TSecJWTSettings(expiryDuration = 10.minutes, maxIdle = None),
      (id: String) => OptionT(db.getUser(id)),
      key
    )

  def jwtToken(login: String, password: String): F[Option[String]] =
    for {
      userOpt  <- db.getUser(login, password)
      response <- userOpt.traverse(user => jwtAuthenticator.create(user.id).map(_.jwt.toEncodedString))
    } yield response
}

object AuthService {
  def mk[F[_]: Sync](db: Db[F]): F[AuthService[F]] =
    for {
      key <- HMACSHA256.generateKey[F]
    } yield new AuthServiceImpl(db, key)
}
