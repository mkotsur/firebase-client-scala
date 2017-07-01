package io.github.mkotsur.firebase.rest

import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory
import configs.syntax._
import io.circe.generic.auto._
import io.github.mkotsur.firebase.auth.AdminCredentials
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.{FunSpec, Matchers, OptionValues, TryValues}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration

class FirebaseClientTest extends FunSpec with Matchers with ScalaFutures with TryValues with OptionValues with Eventually {

  private val config = ConfigFactory.load("application-test.conf")

  implicit val defaultPatience = PatienceConfig(
    config.get[Duration]("patience.timeout").value,
    config.get[Duration]("patience.interval").value
  )

  private val projectId = config.get[String]("firebase.projectId").value

  private val validJsonKey = Files.readAllBytes(
    Paths.get(getClass.getResource("/rest-client-scala-test-b1940c24c184.json").toURI)
  )

  private val inValidJsonKey = Files.readAllBytes(
    Paths.get(getClass.getResource("/rest-client-scala-test-invalid.json").toURI)
  )

  describe("Firebase client") {

    describe("token") {
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

    describe("read") {
      it("should read primitives from Firebase") {
        val adminCredential = AdminCredentials(validJsonKey)
        implicit val token = FirebaseClient.getToken(adminCredential).success.value
        val fc = new FirebaseClient(projectId)
        fc.get[Int]("eternal/shouldReadThis/age").futureValue shouldBe Some(100)
      }

      it("should read objects from Firebase") {
        case class MyUser(name: String, age: Int)

        val adminCredential = AdminCredentials(validJsonKey)
        implicit val token = FirebaseClient.getToken(adminCredential).success.value
        val fc = new FirebaseClient(projectId)
        fc.get[MyUser]("eternal/shouldReadThis").futureValue shouldBe Some(MyUser("Tom", 100))
      }

      it("should return None if requested path does not exist") {
        case class MyUser(name: String, age: Int)

        val adminCredential = AdminCredentials(validJsonKey)
        implicit val token = FirebaseClient.getToken(adminCredential).success.value
        val fc = new FirebaseClient(projectId)
        fc.get[MyUser]("eternal/doesNotExist").futureValue shouldBe None
      }

      it("should read arrays from Firebase") {
        val adminCredential = AdminCredentials(validJsonKey)
        implicit val token = FirebaseClient.getToken(adminCredential).success.value

        val fc = new FirebaseClient(projectId)

        fc.get[List[Int]]("listsEverywhere/listsOfInts").futureValue shouldBe Some(List(1, 2, 3, 5, 10))
      }
    }
 
    describe("filtering") {
        it("should return a filtered map of users") {
          case class MyUser(name: String, age: Int)

          val adminCredential = AdminCredentials(validJsonKey)
          implicit val token = FirebaseClient.getToken(adminCredential).success.value
          val fc = new FirebaseClient(projectId)

          val expected = Map(
            "user1" -> MyUser("John", 42),
            "user3" -> MyUser("Andre", 60)
          )
          val range = Range("name", "A", "K")
          fc.get[Map[String, MyUser]]("filtering/map", range).futureValue shouldBe Some(expected)
        }
      }

      describe("create and remove") {
        it("should create primitives in Firebase") {
          val adminCredential = AdminCredentials(validJsonKey)
          implicit val token = FirebaseClient.getToken(adminCredential).success.value
          val fc = new FirebaseClient(projectId)
          fc.put(42, "temp/answer").futureValue shouldBe Some(42)
          fc.get[Int]("temp/answer").futureValue shouldBe Some(42)
          fc.delete("temp").futureValue shouldBe ((): Unit)
          fc.get[Int]("temp/answer").futureValue shouldBe None
        }

        it("should create objects in Firebase") {
          case class MyCow(name: String)

          val adminCredential = AdminCredentials(validJsonKey)
          implicit val token = FirebaseClient.getToken(adminCredential).success.value
          val fc = new FirebaseClient(projectId)
          fc.put(MyCow("Henrietta"), "temp/cow").futureValue shouldBe Some(MyCow("Henrietta"))
          fc.get[MyCow]("temp/cow").futureValue shouldBe Some(MyCow("Henrietta"))

          fc.delete("temp").futureValue shouldBe ((): Unit)
          fc.get[MyCow]("temp/cow").futureValue shouldBe None
        }
        
      it("should create a list of ints in Firebase") {
        val adminCredential = AdminCredentials(validJsonKey)
        implicit val token = FirebaseClient.getToken(adminCredential).success.value
        val fc = new FirebaseClient(projectId)

        fc.put(List("John", "Angela", "Jake"), "listsEverywhere/listsOfStrings").futureValue shouldBe Some(List("John", "Angela", "Jake"))
        fc.get[List[String]]("listsEverywhere/listsOfStrings").futureValue shouldBe Some(List("John", "Angela", "Jake"))

        fc.delete("listsEverywhere/listsOfStrings").futureValue shouldBe((): Unit)
        fc.get[List[String]]("listsEverywhere/listsOfStrings").futureValue shouldBe None
      }
    }

    describe("update") {
        it("should update values in Firebase") {
          val adminCredential = AdminCredentials(validJsonKey)
          implicit val token = FirebaseClient.getToken(adminCredential).success.value
          val fc = new FirebaseClient(projectId)
          fc.put("First", "temp/something").futureValue shouldBe Some("First")
          fc.get[String]("temp/something").futureValue shouldBe Some("First")

          fc.patch[Map[String, Int]](Map("something" -> 43), "temp/").futureValue shouldBe Map("something" -> 43)
          fc.get[Int]("temp/something").futureValue shouldBe Some(43)
          fc.delete("temp").futureValue shouldBe ((): Unit)
        }

        it("should push values into Firebase") {
          val adminCredential = AdminCredentials(validJsonKey)
          implicit val token = FirebaseClient.getToken(adminCredential).success.value
          val fc = new FirebaseClient(projectId)
          val pushedChildName = fc.post[Int](43, "temp/pushed").futureValue
          pushedChildName should not be empty

          fc.get[Int](s"temp/pushed/$pushedChildName").futureValue shouldBe Some(43)
          fc.delete("temp").futureValue shouldBe ((): Unit)
        }
        
      it("should update the values of an existing list into Firebase") {
        val adminCredential = AdminCredentials(validJsonKey)
        implicit val token = FirebaseClient.getToken(adminCredential).success.value
        val fc = new FirebaseClient(projectId)

        fc.put[List[String]](List("apple", "cherry"), "listsEverywhere/listsOfFruits")

        val currentFruits = eventually {
          val fruitsFound = fc.get[List[String]]("listsEverywhere/listsOfFruits").futureValue
          fruitsFound shouldBe Some(List("apple", "cherry"))
          fruitsFound.value
        }

        fc.put[List[String]](currentFruits ::: List[String]("pear"), "listsEverywhere/listsOfFruits")

        eventually {
          fc.get[List[String]]("listsEverywhere/listsOfFruits").futureValue shouldBe Some(List("apple", "cherry", "pear"))
        }

        fc.delete("listsEverywhere/listsOfFruits").futureValue shouldBe((): Unit)
      }

      describe("exists") {
        it("should return false if the path does not exist in the DB") {
          val adminCredential = AdminCredentials(validJsonKey)
          implicit val token = FirebaseClient.getToken(adminCredential).success.value
          val fc = new FirebaseClient(projectId)

          fc.exists("/does-not-exist").futureValue shouldBe false
          fc.exists("does-not-exist").futureValue shouldBe false
          fc.exists("/does-not-exist/a/b/c").futureValue shouldBe false
          fc.exists("does-not-exist/a/b/c").futureValue shouldBe false

          fc.exists("eternal/shouldReadThis/a/b/c").futureValue shouldBe false
          fc.exists("/eternal/shouldReadThis/a/b/c").futureValue shouldBe false
        }

        it("should return true if the path exists in the DB") {
          val adminCredential = AdminCredentials(validJsonKey)
          implicit val token = FirebaseClient.getToken(adminCredential).success.value
          val fc = new FirebaseClient(projectId)

          fc.exists("eternal/shouldReadThis").futureValue shouldBe true
          fc.exists("/eternal/shouldReadThis").futureValue shouldBe true
        }
      }
    }
  }
}
