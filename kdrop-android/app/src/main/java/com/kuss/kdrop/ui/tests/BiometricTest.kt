package com.kuss.kdrop.ui.tests

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.orhanobut.logger.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricTest(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BiometricTest") },
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
//            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(appIt),
        ) {

            val context = LocalContext.current

            val fragmentActivity = context as FragmentActivity

            Button(onClick = {
                startBiometricPrompt(context, fragmentActivity)
            }) {
                Text(text = "ASD")
            }

        }
    }
}

fun startBiometricPrompt(context: Context, activity: FragmentActivity) {
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate =
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)

    if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
        Logger.d("can")
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Title")
        .setSubtitle("Subtitle")
        .setDescription("Description")
        .setNegativeButtonText("Cancel")
        .build()

    val biometricPrompt =
        BiometricPrompt(activity, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // 处理身份验证成功的情况
                Logger.d("onAuthenticationSucceeded")
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // 处理身份验证错误的情况
                Logger.d("$errorCode, $errString")
            }

            override fun onAuthenticationFailed() {
                Logger.d("onAuthenticationFailed")
                // 处理身份验证失败的情况
            }
        })

    biometricPrompt.authenticate(promptInfo)
}