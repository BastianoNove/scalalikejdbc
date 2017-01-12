import java.sql.{Connection, DriverManager}
import scalikejdbc._


import com.newrelic.api.agent.Trace

import scala.util.Random

object Library {

  def main(args: Array[String]): Unit = {
    Class.forName("org.h2.Driver")
    createDb()
    insertManyRows()
    ConnectionPool.singleton("jdbc:h2:./books", "sa", "")


    while (true) {
      Thread.sleep(2000)
      dbCallInTransaction()
    }


  }

  def createDb(): Unit = {
    val connection = DriverManager.getConnection("jdbc:h2:./books", "sa", "")
    val statement = connection.createStatement()
    statement.execute("create TABLE IF NOT EXISTS books ( title varchar(255), author varchar(255), price int);");
    connection.close()
  }

  def insertManyRows(): Unit = {
    val connection = DriverManager.getConnection("jdbc:h2:./books", "sa", "")
    val randomGenerator = new Random()


    val query = "insert into books (title, author, price) values (?, ?, ?)"
    val prepareStatement = connection.prepareStatement(query)

    1 to 100000 foreach { _ =>
      prepareStatement.setString(1, randomString(11))
      prepareStatement.setString(2, randomString(11))
      prepareStatement.setInt(3, randomGenerator.nextInt(200))
      prepareStatement.execute()
    }

  }

  @Trace(dispatcher = true)
  def dbCallInTransaction(): Unit = {
    val connection = DriverManager.getConnection("jdbc:h2:./books", "sa", "")
    implicit val session = AutoSession
    val numberOfRows = sql"select count(1) from books".execute.apply()
    println("Found ", numberOfRows, "rows")
  }

  def randomString(length: Int) = scala.util.Random.alphanumeric.take(length).mkString
}

class Library {
  def someLibraryMethod(): Boolean = true


}
