package io.github.mkotsur.firebase.rest

import java.nio.file.{Files, Paths}

import com.google.firebase.FirebaseApp
import com.typesafe.config.ConfigFactory
import configs.syntax._
import org.scalatest.TryValues._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers, OptionValues}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class FirebaseUsersTest extends FunSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll {

  private val config = ConfigFactory.load("application-test.conf")

  implicit val defaultPatience = PatienceConfig(
    config.get[Duration]("patience.timeout").value,
    config.get[Duration]("patience.interval").value
  )

  private val validJsonKey = Files.readAllBytes(
    Paths.get(getClass.getResource("/rest-client-scala-test-b1940c24c184.json").toURI)
  )

  private val inValidJsonKey = Files.readAllBytes(
    Paths.get(getClass.getResource("/rest-client-scala-test-invalid.json").toURI)
  )

  describe("Firebase users") {

    it("should be created with a valid token") {
      val clientTry = FirebaseUsers.apply(validJsonKey)
      clientTry shouldBe a[Success[_]]
    }

    it("should return failure when created with invalid JSON") {
      val clientTry = FirebaseUsers.apply("{}".getBytes)
      clientTry shouldBe a [Failure[_]]
      clientTry.failure.exception.getMessage shouldBe "Failed to parse service account: 'project_id' must be set"
    }

    it("should fetch a user") {
      val client = FirebaseUsers.apply(validJsonKey).get
      client.getUser("mike@example.com").futureValue.value.email shouldBe "mike@example.com"
    }

    it("should return None when there is no user with such email") {
      val client = FirebaseUsers.apply(validJsonKey).success.value
      client.getUser("doesnotexist@example.com").futureValue shouldBe None
    }

//    it("should return failure when created with invalid token") {
//      val clientTry = FirebaseUsers.apply(inValidJsonKey)
//      clientTry shouldBe a [Failure[_]]
//      clientTry.failure.exception.getMessage should startWith("problem parsing PRIVATE KEY")
//    }

    it("should create user") {
      val client = FirebaseUsers.apply(validJsonKey).get

      val userPassword = "t0p_s3cr3t"

      val newUserFuture = client.createUser("user_001", "mike+1@example.com", userPassword)
      newUserFuture.futureValue shouldBe FirebaseUser("user_001", "mike+1@example.com")
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    FirebaseApp.getInstance()
    val client = FirebaseUsers.apply(validJsonKey).get
    client.removeUser("user_001").futureValue shouldBe "user_001"
  }
}
