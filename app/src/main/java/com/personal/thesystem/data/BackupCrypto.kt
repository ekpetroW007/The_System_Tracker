package com.personal.thesystem.data

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {
    private const val PREFIX = "THE_SYSTEM_BACKUP_V1:"
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12

    fun isEncrypted(value: String): Boolean = value.startsWith(PREFIX)

    fun encrypt(value: String, password: CharArray): String {
        require(password.size >= 6) { "Password must contain at least six characters" }
        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val iv = ByteArray(IV_BYTES).also(SecureRandom()::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(password, salt), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val packed = ByteBuffer.allocate(salt.size + iv.size + encrypted.size)
            .put(salt).put(iv).put(encrypted).array()
        return PREFIX + Base64.getEncoder().encodeToString(packed)
    }

    fun decrypt(value: String, password: CharArray): String {
        require(isEncrypted(value)) { "Unsupported backup format" }
        val packed = Base64.getDecoder().decode(value.removePrefix(PREFIX))
        require(packed.size > SALT_BYTES + IV_BYTES) { "Invalid backup" }
        val salt = packed.copyOfRange(0, SALT_BYTES)
        val iv = packed.copyOfRange(SALT_BYTES, SALT_BYTES + IV_BYTES)
        val encrypted = packed.copyOfRange(SALT_BYTES + IV_BYTES, packed.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(password, salt), GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    private fun key(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        return try {
            SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded, "AES")
        } finally {
            spec.clearPassword()
            password.fill('\u0000')
        }
    }
}
