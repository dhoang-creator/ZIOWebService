import zio.{Task, ZIO, ZIOAppDefault, ZLayer}

object UserSubscription {

  /**
   * Can we just input the vals from the console in either the case class or the object which will be utilised later
   * It's not as primitive as C wherein you have to create a parser but we do have to keep aware of how the data is being plugged in? Raw data?
   */

  // should a validator be built in with regex patterns for the email address?
  case class User(val name: String = scala.io.StdIn.readLine(), val email: String = scala.io.StdIn.readLine())

  class UserSubscription(emailService: EmailService, userDatabase) {
    def subscribeUser(user: User): Task[Unit] =
      for {
        _ <- emailService.email(user)
        _ <- userDatabase.insert(user)
      } yield ()
  }

  object UserSubscription {
    def create(emailService: EmailService, userDatabase: UserDatabase) =
      new UserSubscription(emailService, userDatabase1)

    val live: ZLayer[EmailService with UserDatabase, Nothing, UserSubscription] =
      ZLayer.fromFunction(create _)
  }

  class EmailService {
    def email(user: User): Task[Unit] =
      ZIO.succeed(s"You've just been subscribed to Rock the JVM. Welcome ${user.name}!").unit
  }

  object EmailService {
    def create(): EmailService = new EmailService
    val live: ZLayer[Any, Nothing, EmailService] =
      ZLayer.succeed(create())
  }

  val subscriptionService = ZIO.succeed(
    UserSubscription.create(
      EmailService.create(),
      UserDatabase.create(
        ConnectionPool.create(10)
      )
    )
  )

  def subscribe(user: User) = for {
    sub <- subscriptionService
    _ <- sub.subscribeUser(user)
  } yield ()
}


