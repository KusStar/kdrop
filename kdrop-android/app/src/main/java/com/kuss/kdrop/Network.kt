package com.kuss.kdrop

import android.text.TextUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


const val Separator = "-kdrop-"

fun uploadFile(
    tempFile: File,
    filename: String,
    checksum: String,
    onProgress: (progress: Double) -> Unit,
    onResult: (e: Exception?, res: Response?) -> Unit,
) {
    val requestBody: RequestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            "$checksum$Separator$filename",
            tempFile
                .asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )
        .build()
    val request: Request = Request.Builder()
        .url("${Globals.apiUrl}/upload")
        .post(ProgressRequestBody(requestBody, object : UploadProgressListener {
            override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
                val progress = (bytesUploaded.toDouble() / totalBytes.toDouble()) * 100
                onProgress(progress)
            }
        }))
        .build()

    val client = createOkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult(e, null)
        }

        override fun onResponse(call: Call, response: Response) {
            onResult(null, response)
        }
    })
}

fun downloadFile(
    secret: String,
    onResult: (e: Exception?, res: Response?) -> Unit,
) {
    val request: Request = Request.Builder()
        .url("${Globals.apiUrl}/download/$secret")
        .get()
        .build()

    val client = createOkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult(e, null)
        }

        override fun onResponse(call: Call, response: Response) {
            onResult(null, response)
        }
    })
}

private class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val listener: UploadProgressListener
) : RequestBody() {

    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = countingSink.buffer()

        requestBody.writeTo(bufferedSink)

        bufferedSink.flush()
    }

    inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {

        private var bytesWritten = 0L

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)

            bytesWritten += byteCount
            listener.onProgress(bytesWritten, contentLength())
        }
    }
}

interface UploadProgressListener {
    fun onProgress(bytesUploaded: Long, totalBytes: Long)
}

fun getHeaderFileName(response: Response): String? {
    var dispositionHeader = response.header("Content-Disposition")
    if (!TextUtils.isEmpty(dispositionHeader)) {
        dispositionHeader!!.replace("attachment;filename=", "")
        dispositionHeader.replace("filename*=utf-8", "")
        val strings = dispositionHeader.split("; ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        if (strings.size > 1) {
            dispositionHeader = strings[1].replace("filename=", "")
            dispositionHeader = dispositionHeader.replace("\"", "")
            return dispositionHeader
        }
        return ""
    }
    return ""
}

private fun createOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .sslSocketFactory(createUnsafeSocketFactory(), createTrustManager())
        .hostnameVerifier { _, _ -> true }
        .build()
}

private fun createUnsafeSocketFactory(): SSLSocketFactory {
    val trustAllCerts = arrayOf<TrustManager>(createTrustManager())
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())
    return sslContext.socketFactory
}

private fun createTrustManager(): X509TrustManager {
    return object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            // Do nothing
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            // Do nothing
        }
    }
}