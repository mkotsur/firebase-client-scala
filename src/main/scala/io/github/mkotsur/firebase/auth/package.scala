package io.github.mkotsur.firebase

package object auth {

  /**
    * A wrapper for service account JSON.
    */
  case class AdminCredentials(serviceAccount: Array[Byte])

  case class AccessToken(value: String)

}
