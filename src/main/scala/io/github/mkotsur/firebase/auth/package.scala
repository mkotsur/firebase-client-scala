package io.github.mkotsur.firebase

package object auth {

  /**
    * Represents a service account JSON with project it
    */
  case class AdminCredentials(serviceAccount: Array[Byte])

  case class AccessToken(value: String)

}
