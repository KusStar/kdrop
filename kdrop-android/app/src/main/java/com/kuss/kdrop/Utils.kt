package com.kuss.kdrop

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder
import java.text.StringCharacterIterator


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


fun getFileName(cr: ContentResolver, uri: Uri, withSize:  Boolean): String {
    var projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

    if (withSize) {
        projection = projection.plus(MediaStore.MediaColumns.SIZE)
    }

    var name = ""

    cr.query(uri, projection, null, null, null)?.use { metaCursor ->
        if (metaCursor.moveToFirst()) {
            name = metaCursor.getString(0).toString()
            if (withSize) {
                name += ", ${formatBytes(metaCursor.getString(1).toLong())}"
            }
        }
    }

    return name
}


fun runOnUi(cb: () -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        runCatching {
            cb()
        }
    }
}

fun runOnIo(cb: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        runCatching {
            cb()
        }
    }
}

fun openUrl(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

fun getLocalIp(ctx: Context): String {
    val wifiManager = ctx.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
    var ipAddress = wifiManager.connectionInfo.ipAddress

    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
        ipAddress = Integer.reverseBytes(ipAddress)
    }

    val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()

    val ipAddressString: String? = try {
        InetAddress.getByAddress(ipByteArray).hostAddress
    } catch (ex: UnknownHostException) {
        Log.e("WIFIIP", "Unable to get host address.")
        null
    }

    return ipAddressString!!
}

fun copyToClipboard(context: Context, content: String) {
    val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("kdrop-copied", content)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(
        context,
        "已复制 $content 到剪贴板",
        Toast.LENGTH_SHORT
    ).show()
}


fun makeTempFile(context: Context, suffix: String = ""): File {
    return File.createTempFile("kdrop", suffix, File(context.filesDir.absolutePath))
}

fun makeCacheFile(context: Context, name: String): File {
    val file = File(context.cacheDir, name)
    file.createNewFile()
    return file
}