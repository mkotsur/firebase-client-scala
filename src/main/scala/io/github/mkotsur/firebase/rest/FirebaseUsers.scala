package io.github.mkotsur.firebase.rest

import java.io.ByteArrayInputStream

import com.google.identitytoolkit.{GitkitClient, GitkitServerException, GitkitUser}
import io.circe.parser.decode
import io.github.mkotsur.firebase.auth.KeyConverter
import io.github.mkotsur.firebase.rest.FirebaseUsers.{FirebaseUser, HashedPassword}
import org.json.JSONException

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object FirebaseUsers {

  case class HashedPassword(hash: Array[Byte], algorithm: String, salt: Option[String])

  object FirebaseUser {
    def fromGitKitUser(user: GitkitUser): FirebaseUser = {
      new FirebaseUser(user.getLocalId, user.getEmail)
    }
  }

  case class FirebaseUser(id: String, email: String, password: Option[String] = None)

  def apply(serviceAccount: Array[Byte]): Try[FirebaseUsers] = {

    for {
      serviceAccountFields <- decode[Map[String, String]](new String(serviceAccount)).toTry
      clientEmail <- Try { serviceAccountFields("client_email") }
      keyBytes <- KeyConverter.jsonToPKCS12(serviceAccount).toTry
    } yield {
      val gitkitClient = GitkitClient.newBuilder()
        .setServiceAccountEmail(clientEmail)
        .setKeyStream(new ByteArrayInputStream(KeyConverter.jsonToPKCS12(serviceAccount).right.get))
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

  def getUsers: Iterator[FirebaseUser] =
    client.getAllUsers.asScala.map(FirebaseUser.fromGitKitUser)

  def getAllUsers: Seq[FirebaseUser] = getUsers.toList

  def getUser(email: String)(implicit ec: ExecutionContext): Future[Option[FirebaseUser]] = Future {
    Option(FirebaseUser.fromGitKitUser(client.getUserByEmail(email)))
  } recoverWith {
    case e: GitkitServerException if e.getCause.isInstanceOf[JSONException] =>
      Future.successful(None)
  }

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
