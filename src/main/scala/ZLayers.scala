import UserSubscription.{EmailService, UserDatabase, UserSubscription}
import zio.{ZIO, ZLayer}

import java.time.Clock
import java.util.Random
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.SECONDS

object ZLayers {

  val connectionPoolLayer: ZLayer[Any, Nothing, ConnectionPool] =
    ZLayer.succeed(ConnectionPool.create(10))
  val databaseLayer: ZLayer[ConnectionPool, Nothing, UserDatabase] =
    ZLayer.fromFunction(UserDatabase.create _)
  val emailServiceLayer: ZLayer[Any, Nothing, EmailService] =
    ZLayer.succeed(EmailService.create())
  val userSubscriptionServiceLayer: ZLayer[UserDatabase with EmailService, Nothing, UserSubscription] =
    ZLayer.fromFunction(UserSubscription.create _)

  val databaseLayerFull: ZLayer[Any, Nothing, UserDatabase] = connectionPoolLayer >>> databaseLayer
  val subscriptionRequirementsLayer: ZLayer[Any, Nothing, UserDatabase with EmailService] = databaseLayerFull ++ EmailServiceLayer
  val userSubscriptionLayer: ZLayer[Any, Nothing, UserSubscription] =
    subscriptionRequirementsLayer >>> userSubscriptionServiceLayer
  val runnableProgram = program_v2.provide(userSubscriptionLayer)

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
}
