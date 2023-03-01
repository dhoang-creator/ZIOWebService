package Connection

import zio.{Task, ZLayer}

// the aim here is to build in the ConnectionPool to be used in the Persistence Layer
object Connection {

  class ConnectionPool(threads: Threads) {
    def connectionMaker(): Task[Unit] = for {

    } yield ()
  }

  object ConnectionPool {
    def create(connectionPool: ConnectionPool) =
      new ConnectionPool(threads)
  }

  val live: ZLayer[ConnectionPool, Nothing, UserDatabase] =
    ZLayer.fromFunction(_.create)
}
