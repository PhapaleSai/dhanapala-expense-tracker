package com.phapalesai.dhanapala.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.crypto.AEADBadTagException

class BackupCryptoTest {

    @Test
    fun `decrypting with the same passphrase returns the original bytes`() {
        val plain = "{\"hello\":\"world\"}".toByteArray(Charsets.UTF_8)
        val encrypted = BackupCrypto.encrypt(plain, "correct-horse-battery-staple")
        val decrypted = BackupCrypto.decrypt(encrypted, "correct-horse-battery-staple")
        assertEquals(String(plain, Charsets.UTF_8), String(decrypted, Charsets.UTF_8))
    }

    @Test
    fun `wrong passphrase fails to decrypt instead of silently returning garbage`() {
        val plain = "sensitive data".toByteArray(Charsets.UTF_8)
        val encrypted = BackupCrypto.encrypt(plain, "right-password")
        assertThrows(AEADBadTagException::class.java) {
            BackupCrypto.decrypt(encrypted, "wrong-password")
        }
    }

    @Test
    fun `same plaintext and passphrase produce different ciphertext each time`() {
        val plain = "same input".toByteArray(Charsets.UTF_8)
        val first = BackupCrypto.encrypt(plain, "password123")
        val second = BackupCrypto.encrypt(plain, "password123")
        assertNotEquals(first.toList(), second.toList())
    }
}
