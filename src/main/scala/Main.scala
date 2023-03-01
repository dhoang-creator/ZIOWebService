import API.UserSubscription.{EmailService, User, UserSubscription, subscribe}
import zio.{Scope, ZIO, ZIOAppDefault, ZLayer}
import Connection.Connection.ConnectionPool
import Connection.UserDatabase
import Service.EmailPreferences

/**
 * The dependency injection for each of the files seems to be all over the place, what can we do to ensure that all bases are covered?
 * Import .*?
 */

object Main extends ZIOAppDefault {

  // how do we have a console equivalent for the below so that the user may be able to input the data -> can we broaden the data types also?
  override def run: ZIO[Scope, Any, Any] = {
    def program = for {
      _ <- subscribe(User("Steve", "steve@hotmail.com"))
      _ <- subscribe(User("John", "john@hotmail.com"))
    } yield ()

    ZLayer
      .make[EmailServiceAPI](
        UserSubscription.live,
        Connection.live,
        Persistence.live,
        EmailPreferences.live
      )
      .build
      .map(_.get[EmailServiceAPI])
      .flatMap(program)
  }
}


