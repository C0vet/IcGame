package com.example.icgame

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp

@Composable
fun TestScreen() {

    var current by remember { mutableStateOf("idle") }

    val sheetId = when (current) {
        "idle" -> R.drawable.ic_boy_idle
        "walk" -> R.drawable.ic_boy_walk
        "jump" -> R.drawable.ic_boy_jump
        "pickup" -> R.drawable.ic_boy_pickup
        else -> R.drawable.ic_boy_attack
    }
    val sheet = ImageBitmap.imageResource(id = sheetId)

    val frames = if (current == "attack") 22 else 25

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1CC8B0), Color(0xFFE5106F))))
            .padding(top = 64.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("idle", "walk", "jump", "pickup", "attack").forEach { name ->
                Button(onClick = { current = name }) {
                    Text(name)
                }
            }
        }

        SpriteAnimation(
            spriteSheet = sheet,
            columns = 5,
            rows = 5,
            totalFrames = frames,
            frameDurationMillis = 50,
            modifier = Modifier.size(300.dp)
        )
    }
}