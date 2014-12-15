
import java.util.Date

import scalikejdbc._
import scalikejdbc.config._

import scala.util.Try

object TestApp extends App {

  // logging settings

  GlobalSettings.loggingSQLAndTime = new LoggingSQLAndTimeSettings(
    enabled = true,
    singleLineMode = true,
    logLevel = 'DEBUG
  )

  // setup

  DBs.setupAll()

  // initial cleanup

  Try {
    DB.autoCommit { implicit session =>
      sql"""DROP TABLE OWNER""".execute.apply()
    }
  }

  Try {
    DB.autoCommit { implicit session => {}
      sql"""DROP TABLE COMPANY""".execute.apply()
    }
  }

  // create tables

  DB.autoCommit { implicit session =>
    sql"""CREATE TABLE COMPANY (id INTEGER PRIMARY KEY, name VARCHAR(30), owner VARCHAR(30), created TIMESTAMP)""".execute.apply()
    sql"""CREATE TABLE OWNER (id INTEGER PRIMARY KEY, name VARCHAR(30), created TIMESTAMP, matched CHAR)""".execute.apply()
  }

  // insert some data

  val companies: Seq[Seq[Any]] = Seq(
    1 :: "CBA"       :: "Adam"    :: new Date :: Nil,
    2 :: "Microsoft" :: "Bill"    :: new Date :: Nil,
    3 :: "Google"    :: "Charlie" :: new Date :: Nil,
    4 :: "Apple"     :: "Don"     :: new Date :: Nil
  )

  val owners: Seq[Seq[Any]] = Seq(
    10 :: "Adam" :: new Date :: "?" :: Nil,
    11 :: "Bill" :: new Date :: "?" :: Nil
  )

  DB.autoCommit { implicit session =>
    sql"""INSERT INTO COMPANY (id, owner, name, created) values (?, ?, ?, ?)""".batch(companies: _*).apply()
    sql"""INSERT INTO OWNER (id, name, created, matched) values (?, ?, ?, ?)""".batch(owners: _*).apply()
  }

  // merge!

  DB.autoCommit { implicit session =>
    sql"""
          MERGE INTO OWNER o
            USING (SELECT id, name, owner, created FROM COMPANY) c
            ON (c.name = o.name)
          WHEN MATCHED THEN
            UPDATE SET o.matched = 'Y'
          WHEN NOT MATCHED THEN
            INSERT (o.id, o.name, o.created, o.matched) VALUES (c.id + 100, c.owner, c.created, 'N')
       """.execute.apply()
  }

  // check

  DB.readOnly { implicit session =>
    sql"""SELECT id, name, created, matched FROM OWNER""".foreach { rs =>
      println(s""">>> ${rs.int("id")}, ${rs.string("name")}, ${rs.date("created")}, ${rs.string("matched")}""")
    }
  }

  // cleanup

  DB.autoCommit { implicit session =>
    sql"""DROP TABLE OWNER""".execute.apply()
    sql"""DROP TABLE COMPANY""".execute.apply()
  }


}


