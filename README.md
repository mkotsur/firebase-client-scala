This is a Scala REST client for [Firebase](https://www.firebase.com/), which is based on [Circe](https://github.com/circe/circe) and is intended for services that don't require realtimeness of Firebase and just need to ger read/write access to the database in a convenient way.

## Documentation

### Authentication

One thing to always remember: in order to use this lib, you need a service account, and your app will always be accessing data on behalf of that service account. Kind of "admin access", so it's responsibility of your app to make sure that all the changes are legitimate.
 
 * [More about Google service accounts](https://developers.google.com/identity/protocols/OAuth2ServiceAccount)
 * [More about Firebase REST API User authentication](https://firebase.google.com/docs/reference/rest/database/user-auth)
  
  
## Creating a client
  
```

```

## Development

The tests are executed against a firebase instance `rest-client-scala-test` (configured in `application-test.conf`).