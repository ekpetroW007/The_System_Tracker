package com.personal.thesystem.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoTest {
    @Test
    fun encryptedBackupRoundTripsAndDoesNotExposeJson() {
        val source = "{\"records\":[{\"date\":\"2026-09-01\"}]}"

        val encrypted = BackupCrypto.encrypt(source, "надёжный пароль".toCharArray())

        assertTrue(BackupCrypto.isEncrypted(encrypted))
        assertNotEquals(source, encrypted)
        assertEquals(source, BackupCrypto.decrypt(encrypted, "надёжный пароль".toCharArray()))
    }

    @Test(expected = Exception::class)
    fun wrongPasswordCannotDecryptBackup() {
        val encrypted = BackupCrypto.encrypt("private", "correct1".toCharArray())

        BackupCrypto.decrypt(encrypted, "wrong12".toCharArray())
    }
}
