package com.kuss.kdrop.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.kuss.kdrop.BuildConfig
import com.kuss.kdrop.ui.pages.About
import com.kuss.kdrop.ui.pages.Home
import com.kuss.kdrop.ui.pages.Settings
import com.kuss.kdrop.ui.pages.TestEntries
import com.kuss.kdrop.ui.pages.tests.BiometricTest
import com.kuss.kdrop.ui.pages.tests.CryptoTest
import com.kuss.kdrop.ui.pages.tests.DbTest
import com.kuss.kdrop.ui.pages.tests.DropTest
import com.kuss.kdrop.ui.pages.tests.LocalTest
import com.kuss.kdrop.ui.pages.tests.RemoteTest

enum class Routes {
    HOME,
    ABOUT,
    SETTINGS,
    REMOTE_TEST,
    CRYPTO_TEST,
    BIOMETRIC_TEST,
    DB_TEST,
    DROP_TEST,
    LOCAL_TEST,
    TEST_ENTRIES,
}

@Composable
fun Router(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.TEST_ENTRIES.name) {
        composable(Routes.HOME) { Home(navController) }
        if (BuildConfig.DEBUG) {
            composable(Routes.REMOTE_TEST) { RemoteTest(navController) }
            composable(Routes.LOCAL_TEST) { LocalTest(navController) }
            composable(Routes.CRYPTO_TEST) { CryptoTest(navController) }
            composable(Routes.BIOMETRIC_TEST) { BiometricTest(navController) }
            composable(Routes.DB_TEST) { DbTest(navController) }
            composable(Routes.DROP_TEST) { DropTest(navController) }
            composable(Routes.ABOUT) { About(navController) }
            composable(Routes.SETTINGS) { Settings(navController) }
            composable(Routes.TEST_ENTRIES) { TestEntries(navController) }
        }
    }
}
