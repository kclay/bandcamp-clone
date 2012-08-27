package security

/**
 * Copyright (C) 2011 Havoc Pennington
 * Licensed under the Apache License, Version 2.0
 */


import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils

import org.mindrot.jbcrypt.BCrypt;


object Algorithms {

  // HMAC SHA-512 hash
  private def signature(message: String, key: String): String = {
    require(key.length > 0)

    val mac = Mac.getInstance("HmacSHA512");
    val signingKey = new SecretKeySpec(key.getBytes("utf-8"), "HmacSHA512");
    mac.init(signingKey);
    val messageBytes = message.getBytes("utf-8");
    val resultBytes = mac.doFinal(messageBytes);

    new String(Hex.encodeHex(resultBytes))
  }

  def hashPassword(password: String, serverWidePasswordSecret: String) = {
    if (password.length == 0)
      throw new IllegalArgumentException("Password may not be zero-length")

    // we add the server's secret key to the password,
    // the idea is to require stealing both the server
    // key and the database, which might raise the bar
    // a bit.
    val intermediate = signature(password, serverWidePasswordSecret)

    BCrypt.hashpw(intermediate, BCrypt.gensalt())
  }

  def checkPassword(password: String, passwordHash: String,
                    serverWidePasswordSecret: String) = {
    val intermediate = signature(password, serverWidePasswordSecret)
    BCrypt.checkpw(intermediate, passwordHash)
  }
}