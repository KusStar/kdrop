package com.kuss.kdrop.ui.pages.tests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kuss.kdrop.Crypto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoTest(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("加密、解密测试") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                },
            )
        },
    ) { appIt ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(appIt),
        ) {

            val focusRequester = FocusRequester()

            var text by remember { mutableStateOf("") }
            var secret by remember { mutableStateOf(ByteArray(0)) }
            var key by remember { mutableStateOf("") }
            var log by remember {
                mutableStateOf("")
            }
            val ss = rememberScrollState()

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Spacer(modifier = Modifier.height(256.dp))
            OutlinedTextField(
                label =  {
                    Text("原文")
                },
                value = text, onValueChange = {
                text = it
            }, modifier = Modifier.focusRequester(focusRequester))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                label =  {
                    Text("密钥")
                },
                value = key, onValueChange = {
                key = it
            })

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                secret = Crypto.encrypt(text.encodeToByteArray(), key)
                log += "加密过程: \n$text -> $secret\n\n"
            }) {
                Text(text = "加密")
            }

            Button(onClick = {
                val t = Crypto.decrypt(secret, key)?.decodeToString()
                log += "解密得到: \n$secret -> $t\n\n"
            }) {
                Text(text = "解密")
            }

            Text(
                log, modifier = Modifier
                    .verticalScroll(ss)
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LaunchedEffect(log) {
                ss.scrollTo(ss.maxValue)
            }

        }
    }

}