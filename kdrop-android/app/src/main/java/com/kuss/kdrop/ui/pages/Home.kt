package com.kuss.kdrop.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kuss.kdrop.ui.Routes
import com.kuss.kdrop.ui.navigate


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
                navController.navigate(Routes.CRYPTO_TEST)
            }) {
                Text(text = "加密、解密测试")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate(Routes.PICKER_TEST)
            }) {
                Text(text = "文件加密上传，下载解密测试")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate(Routes.BIOMETRIC_TEST)
            }) {
                Text(text = "BiometricTest")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate(Routes.DB_TEST)
            }) {
                Text(text = "DbTest")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate(Routes.DROP_TEST)
            }) {
                Text(text = "DropTest")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate(Routes.ABOUT)
            }) {
                Text(text = "About")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate(Routes.SETTINGS)
            }) {
                Text(text = "Settings")
            }
        }
    }

}
