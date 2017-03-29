package io.github.mkotsur.firebase.rest

import com.google.identitytoolkit.GitkitUser

object FirebaseUser {

  case class HashedPassword(hash: Array[Byte], algorithm: String, salt: Option[String])

  def fromGitKitUser(user: GitkitUser): FirebaseUser = {
    new FirebaseUser(user.getLocalId, user.getEmail)
  }
}

case class FirebaseUser(id: String, email: String)