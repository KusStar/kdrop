package com.kuss.kdrop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoTest(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("CryptoTest") }) },
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {

            Button(onClick = {
            }) {
                Text(text = "Encrypt")
            }

            Button(onClick = {
            }) {
                Text(text = "Decrypt")
            }
        }
    }

}