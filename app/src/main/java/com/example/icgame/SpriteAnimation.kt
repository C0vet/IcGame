package com.example.icgame

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

@Composable
fun SpriteAnimation(
    spriteSheet: ImageBitmap,
    columns: Int,
    rows: Int,
    totalFrames: Int, //сколько кадров проигрывать
    frameDurationMillis: Int, //сколько миллисекунд показывать один кадр
    modifier: Modifier = Modifier, //размер и положение на экране
    firstFrame: Int = 0 //с какого кадра начать, по умолчанию с нулевого
) {
//размер одного кадра
    val frameWidth = spriteSheet.width / columns
    val frameHeight = spriteSheet.height / rows
//Счётчик кадров
    val transition = rememberInfiniteTransition(label = "sprite")
    val currentFrame by transition.animateValue(
        initialValue = 0,
        targetValue = totalFrames,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = totalFrames * frameDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "frame"
    )
//Рисование кадра
    Canvas(modifier = modifier) {
        val frameIndex = firstFrame + currentFrame.coerceIn(0, totalFrames - 1)
        val column = frameIndex % columns   // колонка кадра в сетке
        val row = frameIndex / columns      // строка кадра в сетке

        drawImage( //вырезает кусок из листа и рисует его
            image = spriteSheet,
            srcOffset = IntOffset(column * frameWidth, row * frameHeight),
            srcSize = IntSize(frameWidth, frameHeight),
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            filterQuality = FilterQuality.Low //сглаживание легкоепри растягивании
        )
    }
}