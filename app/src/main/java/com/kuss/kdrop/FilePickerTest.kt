package com.kuss.kdrop

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendAndReceiveTest(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FilePickerTest") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                },
            )
        },
    ) { it ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            var loading by remember { mutableStateOf(false) }
            var showSaveFile by remember { mutableStateOf(false) }
            var showFilePicker by remember { mutableStateOf(false) }
            var pickedFile by remember { mutableStateOf("") }

            var showOverlay by remember { mutableStateOf(false) }

            FilePicker(showFilePicker) { uri ->
                showFilePicker = false

                if (uri != null) {
                    val iss = context.contentResolver.openInputStream(uri)

                    val name = getFileName(context.contentResolver, uri)
                    val ext = name.substringAfterLast(".", "")

                    Logger.d("$name, $ext")

                    if (iss != null) {
                        loading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            runCatching {
                                val size = formatBytes(iss.available().toLong())
                                pickedFile = "$name - $size"

                                val enTmpFile = File.createTempFile("kdrop", ".encrypt.$ext")
                                val enOutStream = FileOutputStream(enTmpFile)

                                val hash = Crypto.encrypt(iss, enOutStream, "sec")

                                Logger.d("encrypt done")

                                Logger.d(
                                    "${enTmpFile.name}, ${formatBytes(enTmpFile.length())}"
                                )

                                val requestBody: RequestBody = MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart(
                                        "file",
                                        "$hash|$name|$ext",
                                        enTmpFile
                                            .asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                    )
                                    .build()
                                val request: Request = Request.Builder()
                                    .url("http://localhost:3000/upload")
                                    .post(requestBody)
                                    .build()
                                val client = OkHttpClient()

                                client.newCall(request).enqueue(object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        e.printStackTrace()

                                        enTmpFile.canonicalFile.delete()
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        response.body?.let { it1 -> Logger.d(it1.string()) }

                                        enTmpFile.canonicalFile.delete()
                                    }
                                })


                                loading = false
                            }
                        }
                    }
                }
            }

            Button(onClick = {
                showFilePicker = true
            }) {
                Text(text = "Send a file")
            }

            var anchor by remember {
                mutableStateOf(Offset(0f, 0f))
            }
            var targets by remember {
                mutableStateOf(listOf(Rect(0f, 0f, 0f, 0f)))
            }
            var triggered by remember {
                mutableStateOf(false)
            }

            val haptic = LocalHapticFeedback.current

            Text(
                text = pickedFile,
                textAlign = TextAlign.Center,
                modifier =
                Modifier
                    .onGloballyPositioned {
                        anchor = it.positionInRoot()
                    }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                showOverlay = true
                            },
                            onDragEnd = {
                                Log.d("TEST", "drag end")
                                showOverlay = false
                            },
                            onDragCancel = {
                                Log.d("TEST", "drag cancel")
                            }) { change, dragAmount ->
                            val pos = change.position
                                .plus(anchor)
                                .plus(Offset(-100f, -600f))
                            targets.forEach {
                                if (it.contains(pos)) {
                                    Log.d(
                                        "TEST",
                                        "${change.position}"
                                    )
                                    if (!triggered) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        triggered = true
                                    }
                                }
                            }
                            if (!targets.any {
                                    it.contains(pos)
                                }) {
                                triggered = false
                            }
                        }
                    })

            Button(onClick = {
                showSaveFile = true
            }) {
                Text(text = "Receive a file")
            }

            SaveFile(show = showSaveFile, onFileSelected = { uri ->
                showSaveFile = false
                if (uri != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching {
                            val os = context.contentResolver.openOutputStream(uri)
                        }
                    }
                }
            })

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.drawBehind {
                        drawCircle(
                            Color.Red,
                            radius = size.width / 2 - 5.dp.toPx() / 2,
                        )
                    },
                    color = Color.LightGray,
                    strokeWidth = 5.dp
                )
            }

            if (showOverlay) {
                CustomAlertDialog(onDismiss = {
                    showOverlay = false
                }, onLayout1 = {
                    targets = targets.plus(it.boundsInRoot())
                },
                    onLayout2 = {
                        targets = targets.plus(it.boundsInRoot())
                    }
                ) {

                }
            }

        }
    }

}


@Composable
fun CustomAlertDialog(
    onDismiss: () -> Unit,
    onLayout1: (LayoutCoordinates) -> Unit,
    onLayout2: (LayoutCoordinates) -> Unit,
    onExit: () -> Unit
) {

    Dialog(
        onDismissRequest = { onDismiss() }, properties = DialogProperties(
            dismissOnBackPress = false, dismissOnClickOutside = false
        )
    ) {
        Card(
            //shape = MaterialTheme.shapes.medium,
            shape = RoundedCornerShape(10.dp),
            // modifier = modifier.size(280.dp, 240.dp)
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.Red.copy(alpha = 0.8F)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,

                    ) {

                }

                Text(
                    text = "Lorem Ipsum is simply dummy text",
                    modifier = Modifier.padding(8.dp), fontSize = 20.sp
                )

                Text(
                    text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard",
                    modifier = Modifier.padding(8.dp)
                )

                Row(Modifier.padding(top = 10.dp)) {
                    OutlinedButton(
                        onClick = { onDismiss() },
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1F)
                            .onGloballyPositioned {
                                onLayout1(it)
                            }
                    ) {
                        Text(text = "Cancel")
                    }

                    Button(
                        onClick = { onExit() },
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1F)
                            .onGloballyPositioned {
                                onLayout2(it)
                            }
                    ) {
                        Text(text = "Exit")
                    }
                }


            }
        }
    }
}