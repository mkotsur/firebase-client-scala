package io.github.mkotsur.firebase.rest

import java.io.ByteArrayInputStream

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import io.github.mkotsur.firebase.auth.{AccessToken, AdminCredentials}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.Try
import scalaj.http.Http

object FirebaseClient {

  def getToken(adminCredentials: AdminCredentials): Try[AccessToken] = Try {
    val googleCred = GoogleCredential.fromStream(new ByteArrayInputStream(adminCredentials.serviceAccount))

    val scoped = googleCred.createScoped(Seq(
      "https://www.googleapis.com/auth/firebase.database",
      "https://www.googleapis.com/auth/userinfo.email").asJava)
    scoped.refreshToken()

    AccessToken(scoped.getAccessToken)
  }

}