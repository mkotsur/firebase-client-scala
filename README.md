[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c35a823408f847c6a8b2d9fd4a0f1c17)](https://www.codacy.com/app/miccots/firebase-client-scala?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mkotsur/firebase-client-scala&amp;utm_campaign=Badge_Grade)
[![Build status](https://circleci.com/gh/mkotsur/firebase-client-scala.svg?style=shield)](https://circleci.com/gh/mkotsur/firebase-client-scala)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.mkotsur/firebase-client-scala_2.12/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.mkotsur%22)


⚠️ **This project is not maintained anymore** ⚠️

This is a Scala REST client for [Firebase](https://www.firebase.com/), which is based on [Circe](https://github.com/circe/circe) and is intended for services that don't require realtimeness of Firebase and just need to ger read/write access to the database in a convenient way.

## Features:
* Fetching access token based on admin credentials aka service accounts;
* CRUD operations;
* Listing / adding / removing users [TBD].

## Documentation

### Authentication

One thing to always remember: in order to use this lib, you need a service account, and your app will always be accessing data on behalf of that service account. Kind of "admin access", so it's responsibility of your app to make sure that all the changes are legitimate.

 * [More about Google service accounts](https://developers.google.com/identity/protocols/OAuth2ServiceAccount)
 * [More about Firebase REST API User authentication](https://firebase.google.com/docs/reference/rest/database/user-auth)


### Example

Database operations:

```scala
import java.nio.file.{Files, Paths}
import io.github.mkotsur.firebase.auth.AdminCredentials
import io.github.mkotsur.firebase.rest.FirebaseClient
import io.circe.generic.auto._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

val jsonKey: Array[Byte] = Files.readAllBytes(
    Paths.get(getClass.getResource("/rest-client-scala-test-b1940c24c184.json").toURI)
)

val client = new FirebaseClient("rest-client-scala-test")
implicit val token = FirebaseClient.getToken(AdminCredentials(jsonKey)).get

// Reading a primitive
val ageFuture: Future[Option[Int]] = client.get[Int]("users/001/age")

// Reading an object as a case class
case class User(age: Int)
val userFuture: Future[Option[User]] = client.get[User]("users/001")


// more examples in ./src/test/scala/io/github/mkotsur/firebase/rest/FirebaseClientTest.scala
```

## Adding to your project

```sbt
libraryDependencies += "io.github.mkotsur" %% "firebase-client-scala" % {latest-version}
```

## Development

The tests are executed against a firebase instance `rest-client-scala-test` (configured in `application-test.conf`).

There should be a user: `mike@example.com / 123qwe`.
