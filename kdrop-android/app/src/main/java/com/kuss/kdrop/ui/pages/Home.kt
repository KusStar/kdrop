package com.kuss.kdrop.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
        topBar = { TopAppBar(title = { Text("KDrop - 文件加密传输") }, actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.MoreVert, "backIcon")
            }
        }) },
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            Button(onClick = {
                navController.navigate(Routes.LOCAL_TRANSFER)
            }) {
                Text(text = "P2P点对点传输", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.size(16.dp))

            Button(onClick = {
                navController.navigate(Routes.REMOTE_TRANSFER)
            }) {
                Text(text = "C/S中转文件传输", style = MaterialTheme.typography.titleMedium)
            }

        }
    }

}
