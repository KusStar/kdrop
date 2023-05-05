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
                Text(text = "本地网络传输")
            }

            Spacer(modifier = Modifier.size(8.dp))

            Button(onClick = {
                navController.navigate(Routes.LOCAL_TEST)
            }) {
                Text(text = "远程网络传输")
            }

        }
    }

}
