package com.kuss.kdrop.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kuss.kdrop.BuildConfig
import com.kuss.kdrop.R
import com.kuss.kdrop.openUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(appIt),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp)
            )
            Text(
                text = "Kdrop",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Text(
                text = "绝对安全、简单好用的开源文件传输工具。",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Copyright © 2023 KusStar. ",
                style = MaterialTheme.typography.bodyMedium
            )
            val context = LocalContext.current
            Button(
                onClick = {
                    openUrl("https://github.com/KusStar/kdrop", context)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "查看开源地址")
            }
        }
    }
}