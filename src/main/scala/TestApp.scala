
import java.util.Date

import org.joda.time.DateTime
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
      sql"""DROP TABLE CEO""".execute.apply()
    }
  }

  Try {
    DB.autoCommit { implicit session => {}
      sql"""DROP TABLE COMPANY""".execute.apply()
    }
  }

  // model

  case class Company(id: Long, name: String, owner: String, created: DateTime)
  object Company extends SQLSyntaxSupport[Company] {
    def apply(rs: WrappedResultSet, rn: ResultName[Company]): Company = new Company (
      id = rs.get(rn.id),
      name = rs.get(rn.name),
      owner = rs.get(rn.owner),
      created = rs.get(rn.created)
    )
    //  autoConstruct(rs, rn)
  }

  case class Ceo(id: Long, name: String, created: DateTime, matched: String)
  object Ceo extends SQLSyntaxSupport[Ceo] {
    def apply(rn: ResultName[Ceo])(rs: WrappedResultSet): Ceo = new Ceo(
      id = rs.get(rn.id),
      name = rs.get(rn.name),
      created = rs.get(rn.created),
      matched = rs.get(rn.matched)
    )
  }


  // create tables

  DB.autoCommit { implicit session =>
    sql"""CREATE TABLE COMPANY (id INTEGER PRIMARY KEY, name VARCHAR(30), owner VARCHAR(30), created TIMESTAMP)""".execute.apply()
    sql"""CREATE TABLE CEO (id INTEGER PRIMARY KEY, name VARCHAR(30), created TIMESTAMP, matched CHAR)""".execute.apply()
  }

  withSQL
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
    companies foreach { company =>
      withSQL { insert.into(Company).values(company) }.update.apply()
    }
    owners foreach { owner =>
      withSQL { insert.into(Ceo).values(owner) }.update.apply()
    }
  }

  // merge!

  DB.autoCommit { implicit session =>
    sql"""
          MERGE INTO CEO o
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
    val o = Ceo.syntax("o")
    withSQL {
      select(o.id, o.name, o.created, o.matched).from(Ceo as o)
    }.map {
      rs => rs.string(2) -> rs.string(4)
    }.list.apply().foreach {
      case ((name, matched)) => println(s">>> $name -> $matched")
    }


      //.foreach (println)

//    sql"""SELECT id, name, created, matched FROM CEO""".foreach { rs =>
//      println(s""">>> ${rs.int("id")}, ${rs.string("name")}, ${rs.date("created")}, ${rs.string("matched")}""")
//    }
  }

  // cleanup

  DB.autoCommit { implicit session =>
    sql"""DROP TABLE CEO""".execute.apply()
    sql"""DROP TABLE COMPANY""".execute.apply()
  }


}


