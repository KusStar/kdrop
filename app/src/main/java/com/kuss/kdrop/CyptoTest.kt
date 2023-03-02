package com.kuss.kdrop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoTest(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("CryptoTest") }) },
    ) { appIt ->
        Column(
//            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(appIt),
        ) {

            val focusRequester = FocusRequester()

            var text by remember { mutableStateOf("") }
            var secret by remember { mutableStateOf(ByteArray(0)) }
            var key by remember { mutableStateOf(java.util.UUID.randomUUID().toString().take(16)) }
            var log by remember {
                mutableStateOf("")
            }
            val ss = rememberScrollState()

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            TextField(value = text, onValueChange = {
                text = it
            },  modifier = Modifier.focusRequester(focusRequester))

            TextField(value = key, onValueChange = {
                key = it
            })

            Button(onClick = {
                secret = Crypto.encrypt(text.encodeToByteArray(), key)
                log += "Encrypt: \n$text -> $secret\n\n"
            }) {
                Text(text = "Encrypt")
            }

            Button(onClick = {
                val t = Crypto.decrypt(secret, key)?.decodeToString()
                log += "Decrypt: \n$secret -> $t\n\n"
            }) {
                Text(text = "Decrypt")
            }

            Text(log, modifier = Modifier
                .verticalScroll(ss).fillMaxWidth().padding(16.dp))

            LaunchedEffect(log) {
                ss.scrollTo(ss.maxValue)
            }

        }
    }

}