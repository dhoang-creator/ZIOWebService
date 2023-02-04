import UserSubscription.User
import zio.{Task, ZLayer}
import Connection.{ConnectionPool}

/**
 * Do we need to inject a JDBC Layer here to interact with the data that is being inputted? Slick -> better composability
 * Doobie -> native like SQL queries
 */

// need to be careful when placing code inside new files given that the references to the classes and the methods may be obscured
object Persistence {

  class UserDatabase(connectionPool: ConnectionPool) {
    def insert(user: User): Task[Unit] = for {
      conn <- connectionPool.get
      _ <- conn.runQuery(s"insert into subscribers(name, email) values (${user.name}, ${user.email}")
    } yield ()
  }

  object UserDatabase {
    def create(connectionPool: ConnectionPool) =
      new UserDatabase(connectionPool)

    val live: ZLayer[ConnectionPool, Nothing, UserDatabase] =
      ZLayer.fromFunction(create _)
  }

}
