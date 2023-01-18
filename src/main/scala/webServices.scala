object webServices extends ZIOAppDefault {

  case class User(name: String, email: String)

  class UserSubscription(emailService: EmailService, userDatabase) {
    def subsribeUser(user: User): Task[Unit] =
      for {
        _ <- emailService.email(user)
        _ <- userDatabase.insert(user)
      } yield ()
  }

  object UserSubscription {
    def create(emailService: EmailService, userDatabase: UserDatabase) =
      new UserSubscription(emailService, userDatabase)

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

  class ConnectionPool(nConnections: Int) {
    def get: Task[Connection] =
      ZIO.succeed(println("Acquired connection")) *> ZIO.succeed(Connection())
  }

  object ConnectionPool {
    def create(nConnection: Int) =
      new ConnectionPool(nConnection)
    def live(nConnections: Int): ZLayer[Any, Nothing, ConnectionPool] =
      ZLayer.succeed(create(nConnections))
  }

  case class Connection() {
    def runQuery(query: String): Task[Unit] =
      ZIO.succeed(println(s"Executing query: $query"))
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

  val program = for {
    _ <- subscribe(User("Daniel", "daniel@rockthejvm.com"))
    _ <- subscribe(User("Bon Jovi", "jon@rockthejvm.com"))
  } yield ()

  /**
   * ZLayers
   */
  val connectionPoolLayer: ZLayer[Any, Nothing, ConnectionPool] =
    ZLayer.succeed(ConnectionPool.create(10))
  val databaseLayer: ZLayer[ConnectionPool, Nothing, UserDatabase] =
    ZLayer.fromFunction(UserDatabase.create _)
  val emailServiceLayer: ZLayer[Any, Nothing, EmailService] =
    ZLayer.succeed(EmailService.create())
  val userSubscriptionServiceLayer: ZLayer[UserDatabase with EmailService, Nothing, UserSubscription] =
    ZLayer.fromFunction(UserSubscription.create _)

  val databaseLayerFull: ZLayer[Any, Nothing, UserDatabase] = connectionPoolLayer >>> databaseLaye
  val subscriptinRequirementsLayer: ZLayer[Any, Nothing, UserDatabase with EmailService] = databaseLayerFull ++ EmailServiceLayer
  val userSubscriptionLayer: ZLayer[Any, Nothing, UserSubscription] =
    subscriptionRequirementsLayer >>> userSubscriptionServiceLayer
  val runnableProgram = program_v2.provide(userSubscriptionLayer)

  val runnableProgram_v2 = program_v2.provide(
    UserSubscription.live,
    EmailService.live,
    UserDatabase.live,
    ConnectionPool.live(10),
    ZLayer.Debug.tree
  )

  val userSubscriptionLayer_v2: ZLayer[Any, Nothing, UserSubscription] = ZLayer.make[UserSubscription](
    UserSubscription.live,
    EmailService.live,
    UserDatabase.live,
    ConnectionPool.live(10)
  )

  val dbWithPoolLayer: ZLayer[ConnectionPool, Nothing, ConnectionPool with UserDatabase] = UserDatabase.live.passthrough
  val dbService = ZLayer.service[UserDatabase]
  val subscriptionLaunch: ZIO[EmailService with UserDatabase, Nothing, Nothing] = UserSubscription.live.launch

  val getTime = Clock.currentTime(TimeUnit, SECONDS)
  val randomValue = Random.nextInt
  val sysVariable = System.env("HADOOP HOME")
  val printlnEffect = Console.printline("This is ZIO")

  def run = runnableProgram_v2
}


