package com.kuss.kdrop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Picker(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = { TopAppBar(title = { Text("Picker") }) },
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            var showFilePicker by remember { mutableStateOf(false) }
            var pickedFile by remember { mutableStateOf("") }

            FilePicker(showFilePicker) { uri ->
                showFilePicker = false

                if (uri != null) {
                    val iss = context.contentResolver.openInputStream(uri)
                    val name = getFileName(context.contentResolver, uri)
                    if (iss != null) {
                        val size = formatBytes(iss.available())
                        pickedFile = "$name - $size"
                        iss.close()
                    }
                }
            }

            Button(onClick = {
                showFilePicker = true
            }) {
                Text(text = "Pick a file")
            }

            Text(text = pickedFile, textAlign = TextAlign.Center)
        }
    }

}