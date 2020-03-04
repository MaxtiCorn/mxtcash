package com.maxticorn.db

import cats.effect.Bracket
import com.maxticorn.domain.User
import doobie.implicits._
import doobie.util.transactor.Transactor

trait Db[F[_]] {
  def createUserTable: F[Unit]
  def saveUser(login: String, password: String): F[Long]
  def getUser(id: String): F[Option[User]]
  def getUser(login: String, password: String): F[Option[User]]
}

class DbImpl[F[_]](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable]) extends Db[F] {
  def createUserTable: F[Unit] =
    (for {
      _ <- SQL.createUserTable.run
      _ <- SQL.insertAdminUser.withUniqueGeneratedKeys[Long]("id")
    } yield ()).transact(transactor)

  def saveUser(login: String, password: String): F[Long] =
    SQL.saveUser(login, password).withUniqueGeneratedKeys[Long]("id").transact(transactor)

  def getUser(id: String): F[Option[User]] =
    SQL.getUser(id).option.transact(transactor)

  def getUser(login: String, password: String): F[Option[User]] =
    SQL.getUser(login, password).option.transact(transactor)
}

object Db {
  def apply[F[_]](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable]): Db[F] =
    new DbImpl(transactor)
}

object SQL {
  val createUserTable: doobie.Update0 =
    sql"""create table user(
      id bigint auto_increment,
      login varchar(128) not null,
      password varchar(128) not null
      );
      create index login_index on user(login);
      create index password_index on user(password);""".update

  val insertAdminUser: doobie.Update0 =
    sql"""insert into user(login, password) values ('admin', 'admin')""".update

  def saveUser(login: String, password: String): doobie.Update0 =
    sql"""insert into user(login, password) values ($login, $password)""".update

  def getUser(id: String): doobie.Query0[User] =
    sql"""select * from user where id=$id""".query[User]

  def getUser(login: String, password: String): doobie.Query0[User] =
    sql"""select * from user where login=$login and password=$password""".query[User]
}
