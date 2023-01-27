import UserSubscription.{EmailService, User, UserSubscription, subscribe}
import zio.{ZIOAppDefault, ZLayer}

/**
 * The dependency injection for each of the files seems to be all over the place, what can we do to ensure that all bases are covered?
 * Import .*?
 */

object Main extends ZIOAppDefault{

  // how do we have a console equivalent for the below so that the user may be able to input the data -> can we broaden the data types also?
  val program = for {
    _ <- subscribe(User("Daniel", "daniel@rockthejvm.com"))
    _ <- subscribe(User("Bon Jovi", "jon@rockthejvm.com"))
  } yield ()

  val runnableProgram_v2 = program_v2.provide(
    UserSubscription.live,
    EmailService.live,
    UserDatabase.live,
    ConnectionPool.live(10),
    ZLayer.Debug.tree
  )

  def run = runnableProgram_v2

}
