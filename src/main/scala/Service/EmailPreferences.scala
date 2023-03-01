package Service

import scala.concurrent.ExecutionContext

// note that the below has an Effectual exposure to reading and writing to the DB
object EmailPreferences {

  trait Emails[F[_]]{
    def save(email: Email): F[Either[EmailAlreadyExists.type, Email]]
    def known(email: Email): F[Boolean]
    def findEmail(email: Email): F[Option[Email]]
  }

  class UserRepository(emailRepository: EmailRepository)(
                     implicit ec: ExecutionContext) extends Users[DBIO] {
    override def createUser(primaryEmail: Email, userProfile: UserProfile) = {
      val row = DbUser.from(primaryEmail, userProfile)
      (UsersTable += row).map(PersistedUser(_, row))
    }
    override def identifyUser(email: Email) = identifyQuery(email).result.flatMap{
      case Seq()          => DBIO.successful(None)
      case Seq(singleRow) => DBIO.successful(Some(singleRow))
      case _              => DBIO.failed(
        new IllegalStateException(s"More than one user uses email: $email")
      )
    }
  }

}
