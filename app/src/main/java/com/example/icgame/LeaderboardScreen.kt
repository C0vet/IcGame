package com.example.icgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    var results by remember { mutableStateOf<List<GameResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Загружаем результаты один раз при открытии экрана
    LaunchedEffect(Unit) {
        try {
            val response = ApiClient.getResults()
            if (response.errorMsg.isNullOrEmpty()) {
                // Лучшие результаты сверху
                results = response.results.sortedByDescending { it.score }
            } else {
                errorMsg = response.errorMsg
            }
        } catch (e: Exception) {
            errorMsg = "Не удалось загрузить рекорды. Проверьте интернет."
            println("DEBUG NETWORK ERROR: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1433))
            .statusBarsPadding()          // отступ от выреза камеры и статус-бара
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Таблица лидеров", color = Color.White, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Список занимает всё место между заголовком и кнопкой
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else if (errorMsg != null) {
                Text(errorMsg!!, color = Color.Yellow, fontSize = 18.sp)
            } else if (results.isEmpty()) {
                Text("Пока нет рекордов", color = Color.White, fontSize = 20.sp)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(results) { index, result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. ${result.userName ?: result.project ?: "Игрок"}",
                                color = Color.White,
                                fontSize = 20.sp
                            )
                            Text("${result.score} очков", color = Color.White, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
            Text("Назад в игру")
        }
    }
}