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
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


const val Separator = "-kdrop-"

/**
 * 上传文件的函数
 * @param tempFile 待上传的文件
 * @param filename 文件名
 * @param checksum 文件的 MD5 校验值
 * @param onProgress 上传进度的回调函数，当上传进度发生变化时会调用该函数，参数为上传进度的百分比
 * @param onResult 上传结果的回调函数，上传成功时 e 参数为 null，res 参数为 Response 对象，上传失败时 e 参数为异常对象，res 参数为 null
 */
fun uploadFile(
    tempFile: File,
    filename: String,
    checksum: String,
    onProgress: (progress: Double) -> Unit,
    onResult: (e: Exception?, res: Response?) -> Unit,
) {
    // 组合文件名，格式为 MD5 校验值 + 分隔符 + 文件名
    val fileName = URLEncoder.encode("$checksum$Separator$filename", "UTF-8")
    // 创建 Multipart 请求体
    val requestBody: RequestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            fileName,
            tempFile
                .asRequestBody("multipart/form-data".toMediaTypeOrNull()) // 将文件转换为 RequestBody
        )
        .build()
    // 创建请求对象
    val request: Request = Request.Builder()
        .url("${Globals.apiUrl}/upload") // 设置上传地址
        .post(ProgressRequestBody(requestBody, object : UploadProgressListener { // 设置上传进度监听器
            override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
                val progress = (bytesUploaded.toDouble() / totalBytes.toDouble()) * 100
                onProgress(progress) // 调用上传进度的回调函数
            }
        }))
        .build()

    // 获取 OkHttpClient 对象
    val client = createOkHttpClient()

    // 异步上传文件
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult(e, null) // 调用上传结果的回调函数，传入异常对象和 null
        }

        override fun onResponse(call: Call, response: Response) {
            onResult(null, response) // 调用上传结果的回调函数，传入 null 和 Response 对象
        }
    })
}

data class UploadResponse(
    val status: String,
    val msg: String,
    val data: UploadResData
)

data class UploadResData(
    val file: UploadResFile,
    val secret: String
)

data class UploadResFile(
    val name: String,
    val size: Int,
    val type: String
)

/*
 * 该函数用于从服务器下载文件
 *
 * @param token 包含所下载文件信息的令牌
 * @param onResult 回调函数，用于接收下载结果，包含异常信息和响应内容
 *
 * @return 无返回值
 */
fun downloadFile(
    token: String,// 传入包含了所下载文件信息的令牌
    onResult: (e: Exception?, res: Response?) -> Unit,// 回调函数，用于接收下载结果，包括异常信息和响应内容
) {
    val request: Request = Request.Builder() // 创建请求对象
        .url("${Globals.apiUrl}/download/$token") // 根据令牌构建请求URL
        .get() // 设置为get请求
        .build() // 构建请求对象

    val client = createOkHttpClient() // 创建okhttp客户端

    client.newCall(request).enqueue(object : Callback { // 异步发出请求，并设置回调函数
        override fun onFailure(call: Call, e: IOException) { // 请求失败，触发回调函数
            onResult(e, null) // 将异常信息传递给回调函数
        }

        override fun onResponse(call: Call, response: Response) { // 请求成功，触发回调函数
            onResult(null, response) // 将响应内容传递给回调函数
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
            return URLDecoder.decode(dispositionHeader, "UTF-8")
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