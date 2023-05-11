package com.kuss.kdrop.ui.pages

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.gson.Gson
import com.kuss.kdrop.Crypto
import com.kuss.kdrop.UploadResponse
import com.kuss.kdrop.copyToClipboard
import com.kuss.kdrop.formatBytes
import com.kuss.kdrop.getFileName
import com.kuss.kdrop.getHeaderFileName
import com.kuss.kdrop.makeTempFile
import com.kuss.kdrop.runOnIo
import com.kuss.kdrop.runOnUi
import com.kuss.kdrop.ui.FilePicker
import com.kuss.kdrop.ui.SaveFile
import com.kuss.kdrop.uploadFile
import com.orhanobut.logger.Logger
import okhttp3.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerTransfer(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("C/S 文件服务器传输测试") },
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
            UploadFileComp()
            Spacer(modifier = Modifier.height(16.dp))
            ReceiveFileComp()
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadFileComp() {
    val context = LocalContext.current
    var sendFilePickVisible by remember { mutableStateOf(false) }

    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    var progress by remember {
        mutableStateOf(0f)
    }
    var progressText by remember {
        mutableStateOf("")
    }
    var loading by remember {
        mutableStateOf(false)
    }

    var pickedFile by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf("") }

    var secret by remember {
        mutableStateOf("")
    }

    // 下载加密文件的 token，上传后由服务器返回
    var token by remember {
        mutableStateOf("")
    }

    Button(onClick = {
        openBottomSheet = true
    }) {
        Text(text = "加密文件并上传")
    }

    LaunchedEffect(bottomSheetState.isVisible) {
        if (!bottomSheetState.isVisible) {
            progress = 0f
            progressText = ""
            loading = false
            pickedFile = ""
            fileUri = ""
            secret = ""
            token = ""
        }
    }

    // 加密并上传
    fun encryptAndUpload() {
        progress = 0f
        loading = true

        val uri = fileUri.toUri()

        val name = getFileName(context.contentResolver, uri, false)
        val iss = context.contentResolver.openInputStream(uri)

        if (iss != null) {
            runOnIo {
                progressText = "加密中……"
                progress = -1f
                val enTmpFile = makeTempFile(context,".encrypt")
                val enOutStream = FileOutputStream(enTmpFile)

                val hash = Crypto.encrypt(iss, enOutStream, secret)

                Logger.d("encrypt done")

                Logger.d(
                    "${enTmpFile.name}, ${formatBytes(enTmpFile.length())}"
                )

                progressText = "上传中……"
                uploadFile(enTmpFile, name, hash,
                    onProgress = {
                        progress = (it / 100f).toFloat()
                    }
                ) { e, response ->
                    loading = false

                    if (e != null) {
                        e.printStackTrace()
                        // 清理临时文件
                        enTmpFile.delete()
                        progressText = "上传失败，请检查网络问题"
                        runOnUi {
                            Toast.makeText(
                                context,
                                "上传失败，请检查网络问题",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (response != null) {
                        response.body?.string()?.let {
                            val gson = Gson()
                            val data = gson.fromJson(it, UploadResponse::class.java)
                            token = data.data.secret
                            enTmpFile.delete()
                        }
                        // 清理临时文件
                        progressText = "上传成功！"
                        runOnUi {
                            Toast.makeText(
                                context,
                                "上传成功！",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    if (openBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet = false },
            sheetState = bottomSheetState,
        ) {
            if (token.isNotEmpty()) {

                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("上传成功！", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "该文件的下载+解密口令为：",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    AssistChip(
                        onClick = {
                            copyToClipboard(context, "$token@$secret")
                        },
                        label = {
                            Text(
                                text = "$token@$secret",
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
                    Text(
                        text = "通过此口令可以下载并解密得到原文件",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(64.dp))
                return@ModalBottomSheet
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp, 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(progressText)
                LinearProgressIndicator(
                    modifier = Modifier
                        .semantics(mergeDescendants = true) {}
                        .fillMaxWidth(),
                    progress = progress,
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                if (pickedFile.isNotEmpty() && secret.isNotEmpty()) {
                    Button(
                        onClick = {
                            encryptAndUpload()
                        },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                        enabled = !loading
                    ) {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = "Localized description",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("加密并上传")
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
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
fun ReceiveFileComp() {
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

    var downloadToken by remember {
        mutableStateOf("")
    }
    var token by remember {
        mutableStateOf("")
    }

    var secret by remember {
        mutableStateOf("")
    }

    var loading by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(bottomSheetState.isVisible) {
        if (!bottomSheetState.isVisible) {
            downloadToken = ""
            token = ""
            secret = ""
        }
    }

    fun downloadFile() {
        loading = true
        com.kuss.kdrop.downloadFile(token) { err, res ->
            if (err != null) {
                loading = false
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
                    loading = false
                    return@downloadFile
                }
                saveFileName = getHeaderFileName(res) ?: return@downloadFile
                Logger.d("download done")
                val tempFile = makeTempFile(context, ".decrypt")
                val tfs = FileOutputStream(tempFile)
                try {
                    Crypto.decrypt(res.body!!.byteStream(), tfs, secret)
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
                } finally {
                    loading = false
                }
            }
        }
    }

    Button(onClick = {
        openBottomSheet = true
    }) {
        Text(text = "下载文件并解密")
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
                if (loading) {
                    Text(text = "下载中……")
                    LinearProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Button(onClick = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = clipboard.primaryClip
                    if (clipData != null && clipData.itemCount > 0) {
                        val text = clipData.getItemAt(0).text.toString()
                        downloadToken = text
                        try {
                            token = downloadToken.split("@")[0]
                            secret = downloadToken.split("@")[1]
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }) {
                    Text(text = "从剪贴板导入")
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = downloadToken,
                    onValueChange = {
                        downloadToken = it
                        try {
                            token = downloadToken.split("@")[0]
                            secret = downloadToken.split("@")[1]
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    label = { Text("下载口令") },
                    trailingIcon = {
                        IconButton(onClick = { downloadToken = "" }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "",
                                Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    }
                )


                if (token.isNotEmpty() && secret.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { downloadFile() }) {
                        Text(text = "下载并解密")
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
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
