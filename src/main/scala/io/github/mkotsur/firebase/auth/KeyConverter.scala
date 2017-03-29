package io.github.mkotsur.firebase.auth

import java.io.{ByteArrayOutputStream, StringReader}
import java.net.URL
import java.nio.ByteBuffer
import java.security.{KeyStore, Security}

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter

import scala.language.postfixOps
import scala.util.Try

object KeyConverter {

  Security.addProvider(new BouncyCastleProvider())

  private val KeystorePassword = "notasecret".toCharArray
  private val PrivateKeyAlias = "privatekey"

  case class PemKey(private_key: String, client_email: String, client_x509_cert_url: String)


  def jsonToPKCS12(serviceAccountBytes: Array[Byte]): Either[Throwable, Array[Byte]] = {

    val keyAndCertsEither = for {
      saJson <- io.circe.jawn.parseByteBuffer(ByteBuffer.wrap(serviceAccountBytes))
      pemKey <- implicitly[Decoder[PemKey]].decodeJson(saJson)
      privateKey <- Try({
        val pp = new PEMParser(new StringReader(pemKey.private_key))
        val pki = pp.readObject().asInstanceOf[PrivateKeyInfo]
        val privateKey = new JcaPEMKeyConverter()
          .setProvider("BC")
          .getPrivateKey(pki)
        pp.close()
        privateKey
      }).toEither
      certsMap <- {
        val source = scala.io.Source.fromURL(new URL(pemKey.client_x509_cert_url))
        decode[Map[String, String]](source.mkString)
      }
    } yield (privateKey, certsMap)


    keyAndCertsEither map { case (privateKey, certsMap) =>
      val certs = certsMap.values.map { certBody =>
        val pemParser = new PEMParser(new StringReader(certBody))
        val certHolder = pemParser.readObject().asInstanceOf[X509CertificateHolder]
        val cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder)
        pemParser.close()
        cert
      }
      val baos = new ByteArrayOutputStream
      val ks = KeyStore.getInstance("PKCS12", "BC")
      ks.load(null)

      ks.setKeyEntry(PrivateKeyAlias, privateKey, KeystorePassword, certs.toArray)
      ks.store(baos, KeystorePassword)
      baos.close()
      baos.toByteArray
    }
  }
}