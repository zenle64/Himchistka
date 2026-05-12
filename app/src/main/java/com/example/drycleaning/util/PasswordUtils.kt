package com.example.drycleaning.util

import java.security.MessageDigest

/** Утилита хеширования паролей (SHA-256) */
object PasswordUtils {

    fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, hashedPassword: String): Boolean {
        return hash(password) == hashedPassword
    }
}
