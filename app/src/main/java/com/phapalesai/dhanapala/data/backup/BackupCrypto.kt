package com.phapalesai.dhanapala.data.backup

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-derived AES-GCM encryption for local backup files -- deliberately
 * NOT tied to the Android Keystore, since a Keystore-backed key dies with an
 * uninstall and would make backups unrestorable after exactly the situations
 * (reinstall, new phone) a backup exists to protect against. The salt is
 * stored alongside the ciphertext (it isn't secret); only the passphrase,
 * which never leaves the device, can decrypt the payload.
 */
object BackupCrypto {
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256

    private fun deriveKey(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, ITERATIONS, KEY_LENGTH_BITS)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plainBytes: ByteArray, passphrase: String): ByteArray {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(passphrase.toCharArray(), salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plainBytes)
        return salt + iv + ciphertext
    }

    /** Throws on a wrong passphrase or corrupted file (GCM tag check fails). */
    fun decrypt(payload: ByteArray, passphrase: String): ByteArray {
        require(payload.size > SALT_LENGTH + IV_LENGTH) { "Backup file is too short to be valid." }
        val salt = payload.copyOfRange(0, SALT_LENGTH)
        val iv = payload.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
        val ciphertext = payload.copyOfRange(SALT_LENGTH + IV_LENGTH, payload.size)
        val key = deriveKey(passphrase.toCharArray(), salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return cipher.doFinal(ciphertext)
    }
}
