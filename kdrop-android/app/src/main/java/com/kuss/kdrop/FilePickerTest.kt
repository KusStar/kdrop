package com.kuss.kdrop

import android.util.Log
import android.widget.Toast
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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception


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

                    if (iss != null) {
                        loading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            runCatching {
                                val size = formatBytes(iss.available().toLong())
                                pickedFile = "$name - $size"

                                val enTmpFile = File.createTempFile("kdrop", ".encrypt")
                                val enOutStream = FileOutputStream(enTmpFile)

                                val hash = Crypto.encrypt(iss, enOutStream, "sec")

                                Logger.d("encrypt done")

                                Logger.d(
                                    "${enTmpFile.name}, ${formatBytes(enTmpFile.length())}"
                                )

                                uploadFile(enTmpFile, name, hash) { e, response ->
                                    if (e != null) {
                                        e.printStackTrace()
                                        // 清理临时文件
                                        enTmpFile.canonicalFile.deleteOnExit()
                                    } else if (response != null) {
                                        response.body?.let { it1 -> Logger.d(it1.string()) }
                                        // 清理临时文件
                                        enTmpFile.canonicalFile.deleteOnExit()
                                    }
                                }

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

            var saveFileName by remember {
                mutableStateOf("")
            }
            var tempFilePath by remember {
                mutableStateOf("")
            }

            Button(onClick = {
                downloadFile("leatherjackets-227") { err, res ->
                    if (err != null) {
                        err.printStackTrace()

                    } else if (res?.body != null) {
                        if (res.code != 200) {
                            runOnUi {
                                Toast.makeText(
                                    context,
                                    "出错了，${res.body!!.string()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return@downloadFile
                        }
                        saveFileName = getHeaderFileName(res) ?: return@downloadFile
                        Logger.d("download done")
                        val tempFile = File.createTempFile("kdrop", ".decrypt")
                        val tfs = FileOutputStream(tempFile)
                        try {
                            Crypto.decrypt(res.body!!.byteStream(), tfs, "sec")
                            Logger.d("decrypt done ${tempFile.path}")

                            tempFilePath = tempFile.path
                            showSaveFile = true
                        } catch (e: IOException) {
                            e.printStackTrace()
                            runOnUi {
                                Toast.makeText(
                                    context,
                                    "解密失败，请确保密钥无误",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUi {
                                Toast.makeText(
                                    context,
                                    "出错了，请稍后再试",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

            }) {
                Text(text = "Receive a file")
            }

            SaveFile(show = showSaveFile,
                filename = saveFileName,
                onFileSelected = { uri ->
                    showSaveFile = false
                    if (uri != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            runCatching {
                                val os = context.contentResolver.openOutputStream(uri)
                                val tempFile = File(tempFilePath)
                                val tis = FileInputStream(tempFile)

                                if (os == null) return@runCatching

                                tis.use { input ->
                                    os.use { output ->
                                        input.copyTo(output)
                                    }
                                }

                                os.close()
                                tis.close()
                                runOnUi {
                                    Toast.makeText(context, "下载并解密成功", Toast.LENGTH_SHORT)
                                        .show()
                                }
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