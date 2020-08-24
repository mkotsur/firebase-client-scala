package io.github.mkotsur.firebase

import java.io.ByteArrayInputStream

import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.google.firebase.auth.FirebaseCredentials

object FirebaseAdmin {

  def initialize(serviceAccount: Array[Byte]): FirebaseApp = {
    val credential = FirebaseCredentials.fromCertificate(new ByteArrayInputStream(serviceAccount))

    val options = new FirebaseOptions.Builder()
      .setCredential(credential)
      .build()

    if (FirebaseApp.getApps.isEmpty)
      FirebaseApp.initializeApp(options)
    else
      FirebaseApp.getInstance()
  }

}
