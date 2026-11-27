package com.example.icgame

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.icgame.ui.theme.IcGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IcGameTheme {
                // Переменная для переключения экранов
                var showLeaderboard by remember { mutableStateOf(false) }

                if (showLeaderboard) {
                    LeaderboardScreen(onBack = { showLeaderboard = false })
                } else {
                    GameScreen(onShowLeaderboard = { showLeaderboard = true })
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val goLeft = event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT  //лево
        val goRight = event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT //вправо
        val stop = event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN //вниз

        if (goLeft || goRight || stop) {
            if (event.action == KeyEvent.ACTION_UP) { //ACTION_UP значит что типо клавишу отпустили
                if ((goLeft && InputState.direction == -1) || (goRight && InputState.direction == 1)) {
                    InputState.direction = 0
                }
            } else {
                InputState.direction = when {
                    goLeft -> -1
                    goRight -> 1
                    else -> 0
                }
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}