package io.github.mkotsur.firebase.rest

import java.io.ByteArrayInputStream

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import io.github.mkotsur.firebase.auth.{AccessToken, AdminCredentials}
import io.github.mkotsur.firebase.rest.FirebaseClient.FirebaseClientException

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.Try
import scalaj.http.Http

object FirebaseClient {

  class FirebaseClientException(message: String = "", cause: Throwable = null) extends RuntimeException(message, cause)

  def getToken(adminCredentials: AdminCredentials): Try[AccessToken] = Try {
    val googleCred = GoogleCredential.fromStream(new ByteArrayInputStream(adminCredentials.serviceAccount))

    val scoped = googleCred.createScoped(Seq(
      "https://www.googleapis.com/auth/firebase.database",
      "https://www.googleapis.com/auth/userinfo.email").asJava)
    scoped.refreshToken()

    AccessToken(scoped.getAccessToken)
  }

}

class FirebaseClient(val projectId: String) {

  private val baseUrl = s"https://$projectId.firebaseio.com"

  def get[T](path: String)(implicit decoder: Decoder[T], token: AccessToken): Future[Option[T]] = Future {
    Http(s"$baseUrl/$path.json").param("access_token", token.value).asString
  } flatMap { response =>
    decode[Option[T]](response.body) match {
      case Right(v) => Future.successful(v)
      case Left(e) => Future.failed(e)
    }
  }

  def put[T](data: T, path: String)(implicit token: AccessToken, encoder: Encoder[T], decoder: Decoder[T]): Future[Option[T]] = Future {
    Http(s"$baseUrl/$path.json")
      .param("access_token", token.value)
      .put(encoder(data).toString())
      .header("content-type", "application/json")
      .asString
  } flatMap {
    response => decode[Option[T]](response.body) match {
      case Right(v) => Future.successful(v)
      case Left(e) => Future.failed(e)
    }
  }

  /**
    * Pushes data and returns the child name of the new data.
    */
  def post[T](data: T, path: String)(implicit token: AccessToken, encoder: Encoder[T]): Future[String] = Future {
    Http(s"$baseUrl/$path.json")
      .param("access_token", token.value)
      .postData(encoder(data).toString())
      .header("content-type", "application/json")
      .asString
  } flatMap {
    response => decode[Map[String, String]](response.body) match {
      case Right(v) => Future.successful(v("name"))
      case Left(e) => Future.failed(e)
    }
  }

  /**
    * Updates data and returns the child name of the new data.
    */
  def patch[T](data: T, path: String)(implicit token: AccessToken, encoder: Encoder[T], decoder: Decoder[T]): Future[T] = Future {
    Http(s"$baseUrl/$path.json")
      .param("access_token", token.value)
      .postData(encoder(data).toString())
      .method("PATCH") // should go *after* postData
      .header("content-type", "application/json")
      .asString
  } flatMap {
    response =>
      decode[T](response.body) match {
        case Right(v) => Future.successful(v)
        case Left(e) => Future.failed(new FirebaseClientException(s"Could not decode response ${response.body}", e))
      }
  }

  /**
    * Deletes the data
    */
  def delete(path: String)(implicit token: AccessToken): Future[Unit] = Future {
    Http(s"$baseUrl/$path.json")
      .method("DELETE")
      .param("access_token", token.value)
      .asString
  } flatMap {
    response => response.body match {
      case "null" => Future.successful()
      case body => Future.failed(new FirebaseClientException(s"Expected response [null], but got [$body]"))
    }
  }

}
