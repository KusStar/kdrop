package com.kuss.kdrop.ui.tests

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
import com.kuss.kdrop.Crypto
import com.kuss.kdrop.FilePicker
import com.kuss.kdrop.SaveFile
import com.kuss.kdrop.downloadFile
import com.kuss.kdrop.formatBytes
import com.kuss.kdrop.getFileName
import com.kuss.kdrop.getHeaderFileName
import com.kuss.kdrop.runOnIo
import com.kuss.kdrop.runOnUi
import com.kuss.kdrop.uploadFile
import com.orhanobut.logger.Logger
import okhttp3.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendAndReceiveTest(navController: NavController) {
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
            var pickedFile by remember { mutableStateOf("") }

            var showOverlay by remember { mutableStateOf(false) }

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

            UploadFileComp {
                pickedFile = it
            }

            ReceiveFileComp()

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
fun UploadFileComp(onPick: (file: String) -> Unit) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
    var sendFilePickVisible by remember { mutableStateOf(false) }

    Button(onClick = {
        sendFilePickVisible = true
    }) {
        Text(text = "Send a file")
    }

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

    FilePicker(sendFilePickVisible) { uri ->
        sendFilePickVisible = false

        if (uri != null) {
            val iss = context.contentResolver.openInputStream(uri)

            val name = getFileName(context.contentResolver, uri)

            if (iss != null) {
                loading = true
                runOnIo {
                    val size = formatBytes(iss.available().toLong())
                    onPick("$name - $size")

                    val enTmpFile = File.createTempFile("kdrop", ".encrypt")
                    val enOutStream = FileOutputStream(enTmpFile)

                    val hash = Crypto.encrypt(iss, enOutStream, "sec")

                    Logger.d("encrypt done")

                    Logger.d(
                        "${enTmpFile.name}, ${formatBytes(enTmpFile.length())}"
                    )

                    uploadFile(enTmpFile, name, hash,
                        onProgress = {

                        }
                    ) { e, response ->
                        if (e != null) {
                            e.printStackTrace()
                            // 清理临时文件
                            enTmpFile.canonicalFile.deleteOnExit()
                            runOnUi {
                                Toast.makeText(
                                    context,
                                    "上传失败，请检查网络问题",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (response != null) {
                            response.body?.let { it1 -> Logger.d(it1.string()) }
                            // 清理临时文件
                            enTmpFile.canonicalFile.deleteOnExit()
                            runOnUi {
                                Toast.makeText(
                                    context,
                                    "上传成功",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    loading = false
                }
            }
        }
    }
}


@Composable
fun ReceiveFileComp() {
    val context = LocalContext.current
    var receiveFilePickerVisible by remember { mutableStateOf(false) }
    var saveFileName by remember {
        mutableStateOf("")
    }
    var tempFilePath by remember {
        mutableStateOf("")
    }

    Button(onClick = {
        downloadFile("coachy-141") { err, res ->
            if (err != null) {
                err.printStackTrace()
                runOnUi {
                    Toast.makeText(
                        context,
                        "网络错误：无法连接到服务器",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                    receiveFilePickerVisible = true
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

    // 输入口令弹窗

    SaveFile(show = receiveFilePickerVisible,
        filename = saveFileName,
        onFileSelected = { uri ->
            receiveFilePickerVisible = false
            if (uri != null) {
                runOnIo {
                    val os = context.contentResolver.openOutputStream(uri)
                    val tempFile = File(tempFilePath)
                    val tis = FileInputStream(tempFile)

                    if (os == null) return@runOnIo

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
        })

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