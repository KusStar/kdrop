package com.kuss.kdrop.ui.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.kuss.kdrop.Globals


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
        ) {
            var expanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.TopStart)) {
                SettingsMenuLink(
                    icon = { Icon(imageVector = Icons.Default.Link, contentDescription = "Wifi") },
                    title = { Text(text = "服务器地址") },
                    subtitle = { Text(text = Globals.apiUrl) },
                    onClick = {
                        expanded = true
                    },
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenuItem(
                        text = { Text(Globals.remoteUrl) },
                        onClick = {  },
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("添加") },
                        onClick = { },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}