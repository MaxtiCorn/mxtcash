package com.maxticorn.db

import com.maxticorn.domain.User
import doobie.implicits._

class Db[F[_]] {
  def createTables: doobie.ConnectionIO[Unit] =
    for {
      _ <- SQL.createTables.run
      _ <- SQL.insertAdminUser.withUniqueGeneratedKeys[Long]("id")
    } yield ()

  def getUser(id: String): doobie.ConnectionIO[Option[User]] =
    SQL.getUser(id).option

  def getUser(login: String, password: String): doobie.ConnectionIO[Option[User]] =
    SQL.getUser(login, password).option
}

object SQL {
  val createTables: doobie.Update0 =
    sql"""create table user(
      id bigint auto_increment,
      login varchar(128) not null,
      password varchar(128) not null
      );
      create index login_index on user(login);
      create index password_index on user(password);""".update

  val insertAdminUser: doobie.Update0 =
    sql"""insert into user(login, password) values ('admin', 'admin')""".update

  def getUser(id: String): doobie.Query0[User] =
    sql"""select * from user where id=$id""".query[User]

  def getUser(login: String, password: String): doobie.Query0[User] =
    sql"""select * from user where login=$login and password=$password""".query[User]
}
