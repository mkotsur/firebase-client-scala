package io.github.mkotsur.firebase.rest

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.UserRecord.CreateRequest
import com.google.firebase.auth.{FirebaseAuth, FirebaseAuthException}
import com.google.firebase.tasks.RuntimeExecutionException
import io.github.mkotsur.firebase.FirebaseAdmin
import io.github.mkotsur.firebase.implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.util.Try

object FirebaseUsers {

  /**
    * Initializes [[FirebaseUsers]] client based on JSON service account.
    */
  def apply(serviceAccount: Array[Byte]): Try[FirebaseUsers] = Try {
    new FirebaseUsers(FirebaseAdmin.initialize(serviceAccount))
  }

}

/**
  * This wraps admin client to provide users management capabilities.
  *
  */
class FirebaseUsers(private val app: FirebaseApp) {

  private val auth = FirebaseAuth.getInstance(app)

  /**
    * Returns a future containing either a user or None.
    */
  def getUser(email: String)(implicit ec: ExecutionContext): Future[Option[FirebaseUser]] = {
    auth.getUserByEmail(email).map { userRecord =>
      Some(FirebaseUser(userRecord.getUid, userRecord.getEmail))
    }
  } recoverWith {
    case e: RuntimeExecutionException
      if e.getCause.isInstanceOf[FirebaseAuthException] &&
        e.getCause.getMessage.contains("No user record found for the provided email")  =>
      Future.successful(None)
  }

  /**
    * Creates a user. Please consider the following:
    *   - You have to choose id yourself;
    */
  def createUser(id: String, email: String, password: String)
                (implicit ec: ExecutionContext): Future[FirebaseUser] = {


    val user = new CreateRequest()
    user.setUid(id)
    user.setEmail(email)
    user.setPassword(password)

    auth.createUser(user).map { userRecord => FirebaseUser(userRecord.getUid, userRecord.getEmail) }
  }

  /**
    * Removes a user by id
    */
  def removeUser(id: String)(implicit ec: ExecutionContext): Future[String] =
    auth.deleteUser(id).map(_ => id)

}
