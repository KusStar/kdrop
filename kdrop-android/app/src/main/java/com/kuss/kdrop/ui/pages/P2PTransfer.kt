package com.kuss.kdrop.ui.pages

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.kuss.kdrop.Crypto
import com.kuss.kdrop.Globals
import com.kuss.kdrop.copyToClipboard
import com.kuss.kdrop.formatBytes
import com.kuss.kdrop.getFileName
import com.kuss.kdrop.getLocalIp
import com.kuss.kdrop.makeCacheFile
import com.kuss.kdrop.makeTempFile
import com.kuss.kdrop.runOnIo
import com.kuss.kdrop.runOnUi
import com.kuss.kdrop.ui.FilePicker
import com.kuss.kdrop.ui.SaveFile
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PTransfer(navController: NavController) {
    val context = LocalContext.current

    val serverSocket = remember {
        ServerSocket(Globals.socketPort)
    }

    var myIp by remember {
        mutableStateOf("")
    }

    val receivedFiles = remember {
        mutableStateListOf<File>()
    }

    // 启动 ServerSocket 等待接收
    LaunchedEffect(serverSocket) {
        while (isActive) { // continuously listen for incoming connections
            try {
                val socket = withContext(Dispatchers.IO) {
                    serverSocket.accept()
                } // 等待客户端连接
                launch(Dispatchers.IO) {
                    val inputStream = socket.inputStream
                    val buffer = ByteArray(1024)

                    // 读取文件名长度和文件名
                    val nameLength = inputStream.read()
                    val fileNameBytes = ByteArray(nameLength)
                    inputStream.read(fileNameBytes, 0, nameLength)
                    val fileName = fileNameBytes.decodeToString()

                    // 读取文件内容并写入文件
                    val file = makeCacheFile(context, fileName)
                    val fileOutputStream = FileOutputStream(file)

                    inputStream.use { input ->
                        fileOutputStream.use { fileOutput ->
                            while (true) {
                                val count = input.read(buffer)
                                if (count <= 0) break
                                fileOutput.write(buffer, 0, count)
                            }
                            fileOutput.flush()
                        }
                    }

                    socket.close()
                    Logger.d("server receive ${file.path} ${formatBytes(file.length())}")
                    receivedFiles.add(file)
                }
            } catch (e: Exception) {
                break
            }
        }
    }

    // 页面退出前关闭 serverSocket 监听
    DisposableEffect(Unit) {
        onDispose {
            try {
                serverSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果没有权限，请求权限
            ActivityCompat.requestPermissions(
                context as android.app.Activity,
                arrayOf(Manifest.permission.ACCESS_WIFI_STATE), 0
            )
        } else {
            myIp = getLocalIp(context)
        }
    }


    // 保存文件
    var saveFilePickerVisible by remember { mutableStateOf(false) }
    var saveFileName by remember { mutableStateOf("") }
    var decryptFile by remember { mutableStateOf<File?>(null) }

    fun decryptFile(file: File, secret: String) {
        saveFileName = file.name
        runOnIo {
            val tempFile = makeTempFile(context, ".decrypt")
            val tfs = FileOutputStream(tempFile)
            try {
                Crypto.decrypt(file.inputStream(), tfs, secret)
                Logger.d("decrypt done ${tempFile.path}")

                decryptFile = tempFile
                saveFilePickerVisible = true
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

    SaveFile(show = saveFilePickerVisible,
        filename = saveFileName,
        onFileSelected = { uri ->
            saveFilePickerVisible = false
            if (uri != null && decryptFile != null) {
                runOnIo {
                    val os = context.contentResolver.openOutputStream(uri)
                    val tis = FileInputStream(decryptFile)

                    if (os == null) return@runOnIo

                    tis.use { input ->
                        os.use { output ->
                            input.copyTo(output)
                        }
                    }

                    os.close()
                    tis.close()
                    runOnUi {
                        Toast.makeText(context, "解密并保存成功", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("P2P 传输测试") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {

            var tabIndex by remember { mutableStateOf(0) }
            val titles = listOf("发送文件", "接收文件")
            Column {
                TabRow(selectedTabIndex = tabIndex) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                ) {
                    if (tabIndex == 0) {
                        LocalUploadFileComp()
                    } else if (tabIndex == 1) {
                        if (myIp.isNotEmpty()) {
                            Text(
                                text = "本机的 IP 地址为：",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            AssistChip(
                                onClick = {
                                    copyToClipboard(context, myIp)
                                },
                                label = {
                                    Text(
                                        text = myIp,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = "Localized description",
                                        Modifier.size(AssistChipDefaults.IconSize)
                                    )
                                }
                            )

                        }
                        var decryptSecret by remember {
                            mutableStateOf("")
                        }
                        val decryptFile = remember {
                            mutableStateOf<File?>(null)
                        }
                        // 显示接收到的文件
                        if (receivedFiles.isNotEmpty()) {
                            Text(
                                text = "接收到的文件：",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyColumn(
                                Modifier.padding(16.dp),
                            ) {
                                items(items = receivedFiles) { item ->
                                    OutlinedCard(
                                        onClick = {
                                            decryptFile.value = item
                                        }
                                    ) {
                                        ListItem(headlineContent = {
                                            Text(
                                                "$item.encrypt, ${
                                                    formatBytes(
                                                        item.length()
                                                    )
                                                }"
                                            )
                                        },
                                            leadingContent = {
                                                Icon(
                                                    Icons.Filled.Description,
                                                    contentDescription = "Localized description",
                                                )
                                            })
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            if (decryptFile.value != null) {
                                AlertDialog(
                                    onDismissRequest = {
                                        decryptFile.value = null
                                    }) {
                                    Surface(
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .wrapContentHeight(),
                                        shape = MaterialTheme.shapes.large,
                                        tonalElevation = AlertDialogDefaults.TonalElevation
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            OutlinedTextField(
                                                label = { Text(text = "解密密钥") },
                                                value = decryptSecret,
                                                onValueChange = {
                                                    decryptSecret = it
                                                })
                                            Spacer(modifier = Modifier.height(24.dp))
                                            TextButton(
                                                onClick = {
                                                    decryptFile(decryptFile.value!!, decryptSecret)
                                                    decryptFile.value = null
                                                },
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("解密并保存")
                                            }
                                        }
                                    }

                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "还没有接收到文件",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalUploadFileComp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sendFilePickVisible by remember { mutableStateOf(false) }

    var pickedFile by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf("") }

    var secret by remember {
        mutableStateOf("")
    }

    var targetIp by remember {
        mutableStateOf("")
    }

    fun sendToServerSocket(originalName: String, file: File) {
        if (targetIp.isEmpty()) {
            Logger.e("targetIp is empty")
            return
        }

        // 发送
        scope.launch {
            withContext(Dispatchers.IO) {
                val socket = Socket(targetIp, Globals.socketPort) // 连接到指定的服务器
                val outputStream = socket.getOutputStream()

                val fileInput = FileInputStream(file)

                // 获取文件名
                val fileNameBytes = originalName.encodeToByteArray()
                val nameLength = fileNameBytes.size

                // 发送文件名长度和文件名
                outputStream.write(nameLength)
                outputStream.write(fileNameBytes)

                // 发送文件内容
                val buffer = ByteArray(1024)
                outputStream.use { out ->
                    fileInput.use { fi ->
                        while (true) {
                            val count = fi.read(buffer)
                            if (count <= 0) break
                            out.write(buffer, 0, count)
                        }
                        out.flush()
                    }
                }

                socket.close() // 关闭 Socket 连接
            }
        }
    }

    // 加密并上传
    fun encryptAndUpload() {

        val uri = fileUri.toUri()

        val name = getFileName(context.contentResolver, uri, false)
        val iss = context.contentResolver.openInputStream(uri)

        if (iss != null) {
            runOnIo {
                val enTmpFile = makeTempFile(context, ".encrypt")
                val enOutStream = FileOutputStream(enTmpFile)

                Crypto.encrypt(iss, enOutStream, secret)

                Logger.d("encrypt done")

                Logger.d(
                    "${enTmpFile.name}, ${formatBytes(enTmpFile.length())}"
                )

                sendToServerSocket(name, enTmpFile)
            }
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(32.dp, 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "发送给：", color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = targetIp,
            onValueChange = {
                targetIp = it
            },
            label = { Text("目标 IP 地址") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "选择发送的文件：", color = MaterialTheme.colorScheme.secondary)
        if (pickedFile.isNotEmpty()) {
            OutlinedCard(
                onClick = {
                    sendFilePickVisible = true
                }
            ) {
                ListItem(headlineContent = { Text(pickedFile) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = "Localized description",
                        )
                    })
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = {
                        sendFilePickVisible = true
                    },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = "Localized description",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("选择文件")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "输入加密密钥：", color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = secret,
            onValueChange = { secret = it },
            label = { Text("加密密钥") },
            trailingIcon = {
                IconButton(onClick = { secret = "" }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "",
                        Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (pickedFile.isNotEmpty() && secret.isNotEmpty() && targetIp.isNotEmpty()) {
            Button(
                onClick = {
                    encryptAndUpload()
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(
                    Icons.Filled.Description,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("加密并发送")
            }
        }
    }

    FilePicker(sendFilePickVisible) { uri ->
        sendFilePickVisible = false

        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.close()
                fileUri = uri.toString()
                val name = getFileName(context.contentResolver, uri, true)
                pickedFile = name
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUi {
                    Toast.makeText(
                        context,
                        "文件不存在",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalReceiveFileComp(encryptedFile: File) {
    val context = LocalContext.current
    var receiveFilePickerVisible by remember { mutableStateOf(false) }
    var saveFileName by remember {
        mutableStateOf("")
    }
    var tempFilePath by remember {
        mutableStateOf("")
    }

    var openBottomSheet by remember {
        mutableStateOf(false)
    }
    val bottomSheetState = rememberModalBottomSheetState()

    var secret by remember {
        mutableStateOf("")
    }

    LaunchedEffect(bottomSheetState.isVisible) {
        if (!bottomSheetState.isVisible) {
            secret = ""
        }
    }

    fun decryptFile() {
        val iss = FileInputStream(encryptedFile)
        val tempFile = makeTempFile(context, ".decrypt")
        val tfs = FileOutputStream(tempFile)
        try {
            Crypto.decrypt(iss, tfs, secret)
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

    if (openBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet = false },
            sheetState = bottomSheetState,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp, 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                OutlinedTextField(
                    label = { Text("密钥") },
                    value = secret, onValueChange = {
                        secret = it
                    })

                if (secret.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { decryptFile() }) {
                        Text(text = "下载并解密")
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }

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
