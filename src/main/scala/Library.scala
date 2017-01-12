import com.newrelic.api.agent.Trace
import scalikejdbc._

import scala.util.Random

object Library {

  def main(args: Array[String]): Unit = {
    ConnectionPool.singleton("jdbc:h2:./books", "sa", "")

    createDb()
    insertManyRows(100000)


    while (true) {
      Thread.sleep(2000)
      dbCallInTransaction()
    }


  }

  def createDb(): Unit = {
    implicit val session = AutoSession
    sql"create TABLE IF NOT EXISTS books ( title varchar(255), author varchar(255), price int);".execute().apply()
  }

  def insertManyRows(numberOfRowsToInsert: Integer): Unit = {
    if (getNumberOfRows > 0) {
      return
    }

    val randomGenerator = new Random()
    implicit val session = AutoSession

    1 to numberOfRowsToInsert foreach { _ =>
      val title = randomString(11)
      val author = randomString(11)
      val price = randomGenerator.nextInt(200)
      sql"insert into books (title, author, price) values ($title, $author, $price)".update.apply()
    }

  }

  @Trace(dispatcher = true)
  def dbCallInTransaction(): Unit = {
    val numberOfRows = getNumberOfRows
    println(s"Found $numberOfRows rows")
  }

  def getNumberOfRows: Integer = {
    implicit val session = AutoSession
    sql"select count(1) from books".map(rs => rs.int(1)).single().apply().get
  }

  def randomString(length: Int): String = scala.util.Random.alphanumeric.take(length).mkString
}

class Library {
  def someLibraryMethod(): Boolean = true


}
