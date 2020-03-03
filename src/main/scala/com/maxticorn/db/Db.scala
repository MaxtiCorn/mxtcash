package com.maxticorn.db

import com.maxticorn.domain.User
import doobie.implicits._

class Db[F[_]] {
  def createTables: doobie.ConnectionIO[Unit] =
    for {
      _ <- sql"""create table user(
         id bigint auto_increment,
         login varchar(128) not null,
         password varchar(128) not null
         )""".update.run
      _ <- sql"""insert into user(login, password) values ('admin', 'admin')""".update
        .withUniqueGeneratedKeys[Long]("id")
    } yield ()

  def getUser(login: String, password: String): doobie.ConnectionIO[Option[User]] =
    sql"""select * from user where login=$login and password=$password""".query[User].option
}
