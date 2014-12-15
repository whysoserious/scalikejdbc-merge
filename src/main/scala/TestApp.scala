
import scalikejdbc._
import scalikejdbc.config._

object TestApp extends App {

  println("START")

  DBs.setupAll()

  println("END")

}
