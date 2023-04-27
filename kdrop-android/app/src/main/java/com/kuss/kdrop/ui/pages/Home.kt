package com.kuss.kdrop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("KDrop") }) },
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {

            Button(onClick = {
                navController.navigate("crypto")
            }) {
                Text(text = "Encrypt/Decrypt Test")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate("picker")
            }) {
                Text(text = "Send And Receive Test")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate("biometric")
            }) {
                Text(text = "BiometricTest")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate("db")
            }) {
                Text(text = "DbTest")
            }

        }
    }

}