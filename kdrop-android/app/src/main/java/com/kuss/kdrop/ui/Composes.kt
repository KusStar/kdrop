package com.kuss.kdrop.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun SwitchWithLabel(label: String, state: Boolean, onStateChange: (Boolean) -> Unit) {

    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                // This is for removing ripple when Row is clicked
                indication = null,
                role = Role.Switch,
                onClick = {
                    onStateChange(!state)
                }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {

        Text(text = label)
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Switch(
            checked = state,
            onCheckedChange = {
                onStateChange(it)
            }
        )
    }
}


@Composable
fun FilePicker(
    show: Boolean,
    onFileSelected: (Uri?) -> Unit
) {

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
            onFileSelected(result)
        }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch("*/*")
        }
    }
}

@Composable
fun SaveFile(
    show: Boolean,
    filename: String,
    onFileSelected: (Uri?) -> Unit
) {

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("*/*")) { result ->
            onFileSelected(result)
        }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch(filename)
        }
    }
}