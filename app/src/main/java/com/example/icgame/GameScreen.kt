package com.example.icgame

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

// Настройки игры (единицы: dp и секунды)
private const val GRAVITY = 1800f          // как сильно герой падает вниз
private const val BOUNCE_SPEED = 900f      // скорость отскока от платформы
private const val MOVE_SPEED = 320f        // скорость движения влево-вправо
private const val PLATFORM_WIDTH = 90f
private const val PLATFORM_HEIGHT = 14f
private const val CHARACTER_SIZE = 140f
private const val BOTTOM_AREA = 110f       // тротуар внизу экрана (там типо подсказка)
private const val FIRST_FRAME = 6          // какие кадры листа jump проигрываем
private const val FRAME_COUNT = 6

// Платформы чуть темнее, чтобы не бросались в глаза
private val PlatformColor = Color(0xFF7686AE)

// Платформа: x и y левого верхнего угла в мировых координатах (y считается вверх)
private data class Platform(val x: Float, val y: Float, val width: Float)

// Отправка результата на сервер. Ошибку сети ловит, чтобы игра не падала
private suspend fun submitScore(score: Int) {
    try {
        ApiClient.sendResult(score)
    } catch (e: Exception) {
        println("Ошибка отправки: ${e.message}")
    }
}

@Composable
fun GameScreen(onShowLeaderboard: () -> Unit) {
    // Состояние героя
    var x by remember { mutableFloatStateOf(0f) }
    var y by remember { mutableFloatStateOf(0f) }
    var velocityY by remember { mutableFloatStateOf(0f) }
    var facingRight by remember { mutableStateOf(true) }

    // Состояние игры
    var cameraY by remember { mutableFloatStateOf(0f) }
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var round by remember { mutableIntStateOf(0) }
    var hintVisible by remember { mutableStateOf(true) }
    val platforms = remember { mutableStateListOf<Platform>() }

    // Касание экрана левая половина = влево, правая = вправо (емли   телефон)
    var left by remember { mutableStateOf(false) }
    var right by remember { mutableStateOf(false) }

    // скоуп для отправкирезультата
    val scope = rememberCoroutineScope()
    // задача отправки результата после конца игры (чтобы кнопка «Рекорды» могла её дождаться)
    var sendJob by remember { mutableStateOf<Job?>(null) }

    val sheet = ImageBitmap.imageResource(id = R.drawable.ic_boy_jump)

    // игра закончилась: отправляем результат один раз
    LaunchedEffect(gameOver) {
        if (gameOver && score > 0) {
            sendJob = scope.launch { submitScore(score) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1433))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = maxWidth.value
            val viewH = maxHeight.value - BOTTOM_AREA

            // Игровой цикл. Перезапускается при нажатии «Заново» (меняется round)
            LaunchedEffect(w, viewH, round) {
                platforms.clear()
                platforms.add(Platform(0f, 0f, w))
                var topY = 0f
                var bestY = 0f
                x = w / 2 - CHARACTER_SIZE / 2
                y = 0f
                velocityY = BOUNCE_SPEED
                cameraY = 0f
                score = 0
                gameOver = false
                sendJob = null
                hintVisible = true
                InputState.direction = 0

                var last = withFrameNanos { it }
                while (true) {
                    val now = withFrameNanos { it }
                    val dt = ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                    last = now
                    if (gameOver) continue

                    // Движение влевовправо: клавиатура ИЛИ касание экрана
                    val keyDir = InputState.direction
                    val moveRight = right || keyDir == 1
                    val moveLeft = left || keyDir == -1
                    if (moveRight) {
                        x += MOVE_SPEED * dt
                        facingRight = true
                    }
                    if (moveLeft) {
                        x -= MOVE_SPEED * dt
                        facingRight = false
                    }
                    if (moveRight || moveLeft) hintVisible = false

                    // Вышел за край экрана: появляется с другой стороны
                    if (x > w - CHARACTER_SIZE / 2) x = -CHARACTER_SIZE / 2
                    else if (x < -CHARACTER_SIZE / 2) x = w - CHARACTER_SIZE / 2

                    // Падение и отскок
                    val prevY = y
                    velocityY -= GRAVITY * dt
                    y += velocityY * dt
                    if (velocityY < 0) {
                        val footX = x + CHARACTER_SIZE / 2
                        for (p in platforms) {
                            val crossed = prevY >= p.y && y <= p.y
                            val onPlatform = footX >= p.x - 12f && footX <= p.x + p.width + 12f
                            if (crossed && onPlatform) {
                                y = p.y
                                velocityY = BOUNCE_SPEED
                                break
                            }
                        }
                    }

                    // Камера идёт только вверх
                    val target = y - viewH * 0.4f
                    if (target > cameraY) cameraY = target

                    // Очки за набранную высоту
                    if (y > bestY) bestY = y
                    score = (bestY / 10f).toInt()

                    // Новые платформы сверху
                    val maxGap = (150f + bestY / 100f).coerceAtMost(200f)
                    while (topY < cameraY + viewH + 300f) {
                        topY += 90f + Random.nextFloat() * (maxGap - 90f)
                        val px = Random.nextFloat() * (w - PLATFORM_WIDTH)
                        platforms.add(Platform(px, topY, PLATFORM_WIDTH))
                    }

                    // Старые платформы внизу удаляем
                    platforms.removeAll { it.y < cameraY - 100f }

                    // кпал ниже экрана: конец игры
                    if (y < cameraY - 40f) gameOver = true
                }
            }

            // Фон: здание Айси Групп
            OfficeBackground(
                cameraY = cameraY,
                viewH = viewH,
                modifier = Modifier.fillMaxSize()
            )

            // платформы (земля у входа не рисуется, она часть тротуара)
            platforms.forEach { p ->
                if (p.width >= w) return@forEach
                Box(
                    modifier = Modifier
                        .offset(x = p.x.dp, y = (viewH - (p.y - cameraY)).dp)
                        .size(width = p.width.dp, height = PLATFORM_HEIGHT.dp)
                        .background(PlatformColor, RoundedCornerShape(7.dp))
                )
            }

            // перс.
            SpriteAnimation(
                spriteSheet = sheet,
                columns = 5,
                rows = 5,
                totalFrames = FRAME_COUNT,
                firstFrame = FIRST_FRAME,
                frameDurationMillis = 70,
                modifier = Modifier
                    .offset(
                        x = x.dp,
                        y = (viewH - (y - cameraY) - CHARACTER_SIZE).dp
                    )
                    .size(CHARACTER_SIZE.dp)
                    .graphicsLayer { scaleX = if (facingRight) 1f else -1f }
            )

            // невидимые зоны касания левая и правая половина экрана
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                left = true
                                tryAwaitRelease()
                                left = false
                            })
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                right = true
                                tryAwaitRelease()
                                right = false
                            })
                        }
                )
            }

            // Очки
            Text(
                text = "Очки: $score",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
            )

            // Кнопка «Рекорды»
            Button(
                onClick = {
                    scope.launch {
                        if (gameOver) {
                            // результат уже отправляется ждём но не дольше 2 секунд
                            withTimeoutOrNull(2000) { sendJob?.join() }
                        }
                        // если игра ещё идёт открыват таблицу сразу
                        onShowLeaderboard()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Text("Рекорды")
            }

            // подсказка внизу пропадает когда игрок двигается
            if (hintVisible && !gameOver) {
                Text(
                    text = "Двигайся используя стрелки ← →",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 32.dp)
                )
            }

            // Экран «Игра окончена»
            if (gameOver) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Игра окончена",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Очки: $score",
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Button(
                        onClick = { round++ },
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text("Заново")
                    }
                }
            }
        }
    }
}