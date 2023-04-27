package com.kuss.kdrop.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuss.kdrop.BuildConfig
import com.kuss.kdrop.CryptoTest
import com.kuss.kdrop.Home
import com.kuss.kdrop.ui.tests.BiometricTest
import com.kuss.kdrop.ui.tests.DbTest
import com.kuss.kdrop.ui.tests.SendAndReceiveTest

@Composable
fun Router() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { Home(navController) }
        if (BuildConfig.DEBUG) {
            composable("picker") { SendAndReceiveTest(navController) }
            composable("crypto") { CryptoTest(navController) }
            composable("biometric") { BiometricTest(navController) }
            composable("db") { DbTest(navController) }
        }
    }
}
