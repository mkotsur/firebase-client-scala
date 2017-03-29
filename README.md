[![CircleCI](https://circleci.com/gh/mkotsur/firebase-rest-client-scala.svg?style=svg)](https://circleci.com/gh/mkotsur/firebase-rest-client-scala)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.mkotsur/firebase-client-scala_2.12/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.mkotsur%22)

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
  
  
### Creating a client
  
```

```

### Caveats
 * The library adds BouncyCastle provider at runtime. Not necessarily a bad thing, but something to be aware of;
 * There is a transitive dependency on Guava via `gitkitclient`;
 * Behavior of `FirebaseUsers.createUser` mimics gitkit behavior may be somewhat counter-intuitive. Please make sure to fully understand how it works before using in your app.

## Development

The tests are executed against a firebase instance `rest-client-scala-test` (configured in `application-test.conf`).

There should be a user: `mike@example.com / 123qwe`.