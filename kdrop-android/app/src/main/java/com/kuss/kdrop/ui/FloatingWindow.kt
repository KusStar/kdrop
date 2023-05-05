package com.kuss.kdrop

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kuss.kdrop.ui.Routes
import com.kuss.kdrop.ui.SwitchWithLabel
import com.kuss.kdrop.ui.conditional
import com.kuss.kdrop.ui.navigate
import kotlin.math.roundToInt

// 定义一个悬浮的工具窗口
@Composable
fun FloatingWindow(navController: NavController) {
    //悬浮窗的 UI 样式
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        var hidden by remember {
            mutableStateOf(true)
        }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(400f) }
        val parentWidth = constraints.maxWidth
        val parentHeight = constraints.maxHeight

        Box(
            Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .conditional(!hidden) {
                    padding(32.dp)
                    background(Color(0xAA111111), RoundedCornerShape(8.dp))
                }
                .conditional(hidden) {
                    background(Color(0xAA111111), CircleShape)
                }
                .pointerInput(Unit) {
                    val boxSize = this.size
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(
                            0f,
                            parentWidth - boxSize.width.toFloat()
                        )
                        offsetY = (offsetY + dragAmount.y).coerceIn(
                            0f,
                            parentHeight - boxSize.height.toFloat()
                        )
                    }
                },
        ) {
            Column(
            ) {
                if (hidden) {
                    IconButton(onClick = {
                        hidden = !hidden
                    }) {
                        Icon(Icons.Filled.Face, "backIcon")
                    }
                } else {
                    IconButton(onClick = {
                        hidden = !hidden
                    }) {
                        Icon(Icons.Filled.Close, "backIcon")
                    }
                }
                if (!hidden) {
                    var checked by remember {
                        mutableStateOf(false)
                    }
                    SwitchWithLabel(label = "使用正式接口", state = checked, onStateChange = {
                        checked = it

                        Globals.apiUrl = Globals.getBackendUrl(checked)
                    })

                    Button(onClick = {
                        navController.navigate(Routes.TEST_ENTRIES)
                    }) {
                        Text(text = "测试页面入口")
                    }
                }
            }
        }
    }
}