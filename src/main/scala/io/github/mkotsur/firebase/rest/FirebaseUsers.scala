package io.github.mkotsur.firebase.rest

import java.io.ByteArrayInputStream

import com.google.identitytoolkit.{GitkitClient, GitkitServerException, GitkitUser}
import io.circe.parser.decode
import io.github.mkotsur.firebase.auth.KeyConverter
import io.github.mkotsur.firebase.rest.FirebaseUser.HashedPassword
import org.json.JSONException

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object FirebaseUsers {

  /**
    * Initializes [[FirebaseUsers]] client based on JSON service account.
    */
  def apply(serviceAccount: Array[Byte]): Try[FirebaseUsers] = {
    for {
      serviceAccountFields <- decode[Map[String, String]](new String(serviceAccount)).toTry
      clientEmail <- Try { serviceAccountFields("client_email") }
      keyBytes <- KeyConverter.jsonToPKCS12(serviceAccount).toTry
    } yield {
      val gitkitClient = GitkitClient.newBuilder()
        .setServiceAccountEmail(clientEmail)
        .setKeyStream(new ByteArrayInputStream(keyBytes))
        .setWidgetUrl(null)
        .setCookieName("gtoken")
        .build()
      new FirebaseUsers(gitkitClient)
    }
  }

}

/**
  * This wraps Gitkit client to provide users management capabilities. Most likely, in the future it can be replaced with
  * firebase-admin client for Java: https://firebase.google.com/docs/admin/setup, which currently lacks user management
  * functionality.
  *
  */
class FirebaseUsers(val client: GitkitClient) {

  /**
    * Returns a lazy iterator of all users
    */
  def getUsers: Iterator[FirebaseUser] =
    client.getAllUsers.asScala.map(FirebaseUser.fromGitKitUser)

  /**
    * Returns a list of all users
    */
  def getAllUsers: Seq[FirebaseUser] = getUsers.toList

  /**
    * Returns a future containing either a user or None.
    */
  def getUser(email: String)(implicit ec: ExecutionContext): Future[Option[FirebaseUser]] = Future {
    Option(FirebaseUser.fromGitKitUser(client.getUserByEmail(email)))
  } recoverWith {
    case e: GitkitServerException if e.getCause.isInstanceOf[JSONException] =>
      Future.successful(None)
  }

  /**
    * Creates a user. Please consider the following:
    *   - You have to choose id yourself;
    *   - Gitkit does allow to create multiple users with the same email address;
    *   - Email/password authentication has to be enable at your Firebase project;
    *   -
    */
  def createUser(id: String, email: String, hashedPassword: HashedPassword, hashKey: String)
                (implicit ec: ExecutionContext): Future[FirebaseUser] = {
    val user = new GitkitUser()
    user.setLocalId(id)
    user.setEmail(email)
    user.setHash(hashedPassword.hash)
    hashedPassword.salt.foreach { s => user.setSalt(s.getBytes) }

    Try(client.uploadUsers(hashedPassword.algorithm, hashKey.getBytes, List(user).asJava)) match {
      case Success(_) => Future.successful(FirebaseUser(id, email))
      case Failure(e) => Future.failed(e)
    }
  }

  def removeUser(id: String)(implicit ec: ExecutionContext): Future[String] = Future {
    client.deleteUser(id)
    id
  }

}
