package com.kuss.kdrop

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE)
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