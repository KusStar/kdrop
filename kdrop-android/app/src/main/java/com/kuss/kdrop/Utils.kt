package com.kuss.kdrop

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.text.StringCharacterIterator
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Composable
fun FilePicker(
    show: Boolean,
    onFileSelected: (Uri?) -> Unit
) {
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
            onFileSelected(result)
        }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch("*/*")
        }
    }
}

@Composable
fun SaveFile(
    show: Boolean,
    onFileSelected: (Uri?) -> Unit
) {
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("*/*")) { result ->
            onFileSelected(result)
        }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch("*/*")
        }
    }
}

fun formatBytes(input: Long): String {
    var bytes = input
    if (-1000 < bytes && bytes < 1000) {
        return "$bytes B"
    }
    val ci = StringCharacterIterator("kMGTPE")
    while (bytes <= -999950 || bytes >= 999950) {
        bytes /= 1000
        ci.next()
    }
    val final = bytes / 1000.0
    return String.format("%.${if (final >= 100) 0 else 2}f %cB", final, ci.current())
}

fun getFileName(cr: ContentResolver, uri: Uri): String {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    var name = ""

    cr.query(uri, projection, null, null, null)?.use { metaCursor ->
        if (metaCursor.moveToFirst()) {
            name = metaCursor.getString(0).toString()
        }
    }

    return name
}


class Crypto {
    companion object {
        fun decrypt(data: ByteArray, key: String): ByteArray? {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = IvParameterSpec(ByteArray(16))
            val k = SecretKeySpec(key.padEnd(16, 'a').toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, k, iv)
            return try {
                cipher.doFinal(data)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                null
            }
        }

        fun encrypt(data: ByteArray, key: String): ByteArray {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = IvParameterSpec(ByteArray(16))
            val k = SecretKeySpec(key.padEnd(16, 'a').toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, k, iv)
            return cipher.doFinal(data)
        }

        fun encrypt(
            iss: InputStream,
            fos: OutputStream,
            key: String
        ): String {
            val digest = MessageDigest.getInstance("MD5")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = IvParameterSpec(ByteArray(16))
            val k = SecretKeySpec(key.padEnd(16, 'a').toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, k, iv)

            val cis = CipherInputStream(iss, cipher)
            val buffer = ByteArray(1024 * 2)
            var length: Int
            while (cis.read(buffer).also { length = it } != -1) {
                fos.write(buffer, 0, length)
                digest.update(buffer, 0, length);
            }
            fos.close()
            cis.close()

            val md5Bytes = digest.digest()
            return convertHashToString(md5Bytes)
        }

        private fun convertHashToString(md5Bytes: ByteArray): String {
            var returnVal = ""
            for (i in md5Bytes.indices) {
                returnVal += ((md5Bytes[i].toInt() and 0xff) + 0x100).toString(16)
                    .substring(1)
            }
            return returnVal.uppercase(Locale.getDefault())
        }

        fun decrypt(iss: InputStream, fos: FileOutputStream, key: String) {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = IvParameterSpec(ByteArray(16))
            val k = SecretKeySpec(key.padEnd(16, 'a').toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, k, iv)

            val cos = CipherOutputStream(fos, cipher)
            val buffer = ByteArray(1024)
            var i: Int
            while (iss.read(buffer).also { i = it } != -1) {
                cos.write(buffer, 0, i)
            }
            cos.close()
        }

        fun checksum(data: InputStream): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(1024)
            var read = data.read(buffer, 0, 1024)
            while (read > -1) {
                digest.update(buffer, 0, read)
                read = data.read(buffer, 0, 1024)
            }
            return digest.digest().fold("") { str, it -> str + "%02x".format(it) }
        }

        fun osToIs(data: ByteArrayOutputStream): ByteArrayInputStream {
            return ByteArrayInputStream(data.toByteArray())
        }
    }
}

