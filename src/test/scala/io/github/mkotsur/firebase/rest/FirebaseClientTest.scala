package io.github.mkotsur.firebase.rest

import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory
import configs.syntax._
import io.github.mkotsur.firebase.auth.AdminCredentials
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.TryValues._

class FirebaseClientTest extends FunSpec with Matchers {

  private val config = ConfigFactory.load("application-test.conf")
  private val projectId = config.get[String]("firebase.projectId").value

  private val validJsonKey = Files.readAllBytes(
    Paths.get(getClass.getResource("/rest-client-scala-test-b1940c24c184.json").toURI)
  )

  private val inValidJsonKey = Files.readAllBytes(
    Paths.get(getClass.getResource("/rest-client-scala-test-invalid.json").toURI)
  )

  describe("Firebase client") {

    it("should fetch access token based on service account private key") {
      val adminCredential = AdminCredentials(validJsonKey)
      val token = FirebaseClient.getToken(adminCredential)
      token.success.value.value should not be empty
    }

    it("should return an error when trying to fetch access token if service account is incorrect") {
      val adminCredential = AdminCredentials(inValidJsonKey)
      val token = FirebaseClient.getToken(adminCredential)
      token should not be null
    }
  }

}
