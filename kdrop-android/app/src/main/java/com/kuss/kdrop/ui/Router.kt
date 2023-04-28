package com.kuss.kdrop.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.kuss.kdrop.BuildConfig
import com.kuss.kdrop.ui.pages.About
import com.kuss.kdrop.ui.pages.Home
import com.kuss.kdrop.ui.pages.Settings
import com.kuss.kdrop.ui.pages.tests.BiometricTest
import com.kuss.kdrop.ui.pages.tests.CryptoTest
import com.kuss.kdrop.ui.pages.tests.DbTest
import com.kuss.kdrop.ui.pages.tests.DropTest
import com.kuss.kdrop.ui.pages.tests.SendAndReceiveTest

enum class Routes {
    HOME,
    PICKER_TEST,
    CRYPTO_TEST,
    BIOMETRIC_TEST,
    DB_TEST,
    DROP_TEST,
    ABOUT,
    SETTINGS
}

@Composable
fun Router() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME.name) {
        composable(Routes.HOME) { Home(navController) }
        if (BuildConfig.DEBUG) {
            composable(Routes.PICKER_TEST) { SendAndReceiveTest(navController) }
            composable(Routes.CRYPTO_TEST) { CryptoTest(navController) }
            composable(Routes.BIOMETRIC_TEST) { BiometricTest(navController) }
            composable(Routes.DB_TEST) { DbTest(navController) }
            composable(Routes.DROP_TEST) { DropTest(navController) }
            composable(Routes.ABOUT) { About(navController) }
            composable(Routes.SETTINGS) { Settings(navController) }
        }
    }
}
