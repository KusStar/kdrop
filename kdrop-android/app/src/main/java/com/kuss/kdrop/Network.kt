package com.kuss.kdrop

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.lang.Exception


const val Separator = "-kdrop-"

fun uploadFile(
    tempFile: File, filename: String, checksum: String,
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
        .post(requestBody)
        .build()

    val client = OkHttpClient()

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

    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult(e, null)
        }

        override fun onResponse(call: Call, response: Response) {
            onResult(null, response)
        }
    })
}