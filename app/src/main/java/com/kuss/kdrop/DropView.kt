package com.kuss.kdrop

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DropView() {
   Column(
      horizontalAlignment = Alignment.CenterHorizontally
   ) {
      Box(
         modifier = Modifier.size(320.dp).clip(CircleShape).border(0.1.dp, Color.Gray, CircleShape),
         contentAlignment = Alignment.Center
      ) {
         Box(
            modifier = Modifier.size(240.dp).clip(CircleShape).border(0.1.dp, Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
         ) {
            Box(
               modifier = Modifier.size(160.dp).clip(CircleShape).border(0.1.dp, Color.Gray, CircleShape),
               contentAlignment = Alignment.Center
            ) {
               Box(
                  modifier = Modifier.size(80.dp).clip(CircleShape).border(0.1.dp, Color.Gray, CircleShape),
                  contentAlignment = Alignment.Center
               ) {
                  Image(painter = painterResource(id = R.mipmap.ic_launcher_foreground), contentDescription = "",
                     modifier = Modifier.size(48.dp))
               }
            }
         }
      }
   }
}

@Preview
@Composable
fun DropViewPreview() {
   DropView()
}
