package io.github.mkotsur.firebase.auth

import java.io.ByteArrayInputStream
import java.nio.file.{Files, Paths}
import java.security.Security

import com.google.identitytoolkit.GitkitClient
import io.circe.ParsingFailure
import org.scalatest.{EitherValues, FunSpec, Matchers}

class KeyConverterTest extends FunSpec with Matchers with EitherValues {

  private val validJsonKey = Files.readAllBytes(
    Paths.get(getClass.getResource("/rest-client-scala-test-b1940c24c184.json").toURI)
  )

  describe("Firebase key converter") {

    it("should register bouncycastle provider") {
      val _ = KeyConverter
      Security.getProvider("BC") should not be null
    }

    it("should properly convert JSON/PEM to P12") {
      val keyBytesEither = KeyConverter.jsonToPKCS12(validJsonKey)
      val keyBytes = keyBytesEither.right.value

      val client = GitkitClient.newBuilder()
        .setServiceAccountEmail("firebase-adminsdk-j28gm@rest-client-scala-test.iam.gserviceaccount.com")
        .setKeyStream(new ByteArrayInputStream(keyBytes))
        .build()

      val user = client.getUserByEmail("mike@example.com")
      user.getEmail shouldBe "mike@example.com"

    }

    it("should return error if the key can not be converted") {
      val keyBytesEither = KeyConverter.jsonToPKCS12("{nonsense}".getBytes)
      val error = keyBytesEither.left.value

      error shouldBe a [ParsingFailure]
    }
  }

}
