package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.data.security.AES256CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Swasthya Notes", appName)
    }

    @Test
    fun `test AES256 encryption and decryption roundtrip`() {
        val originalData = "BAMS Rachana Sharira Shloka and Anatomy Notes".toByteArray(Charsets.UTF_8)
        val encrypted = AES256CryptoManager.encrypt(originalData)
        assertTrue(encrypted.isNotEmpty())

        val decrypted = AES256CryptoManager.decrypt(encrypted)
        val decryptedString = String(decrypted, Charsets.UTF_8)
        assertEquals("BAMS Rachana Sharira Shloka and Anatomy Notes", decryptedString)
    }

    @Test
    fun `test UserRole and ContentType enum properties`() {
        assertEquals(2, UserRole.values().size)
        assertEquals(UserRole.OWNER, UserRole.valueOf("OWNER"))
        assertEquals(UserRole.STUDENT, UserRole.valueOf("STUDENT"))

        val pdfType = ContentType.PDF_NOTE
        assertEquals(ContentType.PDF_NOTE, pdfType)
    }
}
