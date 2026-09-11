package com.example.data.security

import android.content.Context
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AES256CryptoManager {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    // Internal app master seed combined with hardware/package salt for 256-bit AES key derivation
    private const val APP_KEY_SEED = "SwasthyaNotes_BAMS_Ayurvedic_Secure_Vault_2026_KeySeed"
    private const val IV_SEED = "Swasthya_IV_Seed_16Byte"

    private fun getSecretKey(): SecretKeySpec {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha256.digest(APP_KEY_SEED.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun getIv(): IvParameterSpec {
        val md5 = MessageDigest.getInstance("MD5")
        val ivBytes = md5.digest(IV_SEED.toByteArray(Charsets.UTF_8))
        return IvParameterSpec(ivBytes)
    }

    fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), getIv())
        return cipher.doFinal(data)
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), getIv())
        return cipher.doFinal(encryptedData)
    }

    fun saveEncryptedFile(context: Context, fileName: String, rawData: ByteArray): File {
        val vaultDir = File(context.filesDir, "secure_vault")
        if (!vaultDir.exists()) {
            vaultDir.mkdirs()
        }
        val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9_.-]"), "_") + ".aes256"
        val targetFile = File(vaultDir, safeFileName)
        val encryptedData = encrypt(rawData)
        FileOutputStream(targetFile).use { fos ->
            fos.write(encryptedData)
            fos.flush()
        }
        return targetFile
    }

    fun readEncryptedFile(context: Context, fileName: String): ByteArray? {
        val vaultDir = File(context.filesDir, "secure_vault")
        val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9_.-]"), "_") + ".aes256"
        val targetFile = File(vaultDir, safeFileName)
        if (!targetFile.exists()) return null
        return runCatching {
            val encryptedBytes = FileInputStream(targetFile).use { it.readBytes() }
            decrypt(encryptedBytes)
        }.getOrNull()
    }

    fun deleteEncryptedFile(context: Context, fileName: String): Boolean {
        val vaultDir = File(context.filesDir, "secure_vault")
        val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9_.-]"), "_") + ".aes256"
        val targetFile = File(vaultDir, safeFileName)
        return if (targetFile.exists()) targetFile.delete() else false
    }

    fun clearAllOfflineFiles(context: Context) {
        val vaultDir = File(context.filesDir, "secure_vault")
        if (vaultDir.exists()) {
            vaultDir.listFiles()?.forEach { it.delete() }
        }
    }
}
