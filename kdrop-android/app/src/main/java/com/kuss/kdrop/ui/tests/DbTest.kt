package com.kuss.kdrop.ui.tests

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.room.Room
import com.kuss.kdrop.db.AppDatabase
import com.kuss.kdrop.db.User
import com.kuss.kdrop.runOnIo
import com.orhanobut.logger.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbTest(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DbTest") },
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

            val context = LocalContext.current

            val db = remember {
                Room.databaseBuilder(context, AppDatabase::class.java, "kdrop").build()
            }
            val userDao = db.userDao()


            Button(onClick = {
                runOnIo {
                    userDao.insert(User(name = "asd", age = 12))
                }
            }) {
                Text(text = "Insert")
            }

            Button(onClick = {
                runOnIo {
                    Logger.d(userDao.getAll())
                }
            }) {
                Text(text = "Get All")
            }

        }
    }
}