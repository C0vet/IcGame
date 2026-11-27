package com.example.icgame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private const val GROUND_H = 130f // выс  первого этажа (с входом)
private const val FLOOR_H = 96f   // выс обычного этажа
private const val BALCONY_W = 30f // на сколько балкон выступает влево
private const val SIGN_W = 300f  // шир вывески
private const val SIGN_H = 46f // выс вывески
private const val SIGN_BOTTOM = 146f  // на какой высоте нижний край вывески
@Composable
fun OfficeBackground(cameraY: Float, viewH: Float, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val widthDp = maxWidth.value

        Canvas(modifier = Modifier.fillMaxSize()) {
            val u = 1.dp.toPx() // сколько пикселей в одном dp
            val w = size.width / u // шир экрана в dp
            val h = size.height / u  // высота

            // Прямоугольник: координаты и размеры в dp, y считается сверху вниз
            fun rect(color: Color, x: Float, y: Float, rw: Float, rh: Float) {
                drawRect(color, Offset(x * u, y * u), Size(rw * u, rh * u))
            }
            // Высота в мире - координата на экране
            fun sy(worldY: Float): Float = viewH - (worldY - cameraY)
 //небо
            val f = (cameraY / 4000f).coerceIn(0f, 1f)
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        lerp(Color(0xFF1B2A5C), Color(0xFF05081A), f),
                        lerp(Color(0xFF6A4C93), Color(0xFF0E1433), f),
                        lerp(Color(0xFFFF9A7A), Color(0xFF1A1F4A), f)
                    )
                )
            )
//звезды
            if (f > 0.15f) {
                for (i in 0 until 40) {
                    val sx = ((i * 7919) % 1000) / 1000f * w
                    val sY = ((i * 104729) % 1000) / 1000f * h
                    drawCircle(Color(0xFFDDE6FF).copy(alpha = f), 1.2f * u, Offset(sx * u, sY * u))
                }
            }
//здание и панели
            val bx = w * 0.14f
            val bw = w * 0.82f
            val groundY = sy(0f)
            val baseBottom = min(groundY, h)
            rect(Color(0xFF14203F), bx, 0f, bw, baseBottom)

            val panelPos = floatArrayOf(0f, 0.21f, 0.44f, 0.63f, 0.88f)
            val panelW = floatArrayOf(14f, 20f, 12f, 22f, 14f)
            for (i in panelPos.indices) {
                val c = if (i % 2 == 0) Color(0xFF3A4766) else Color(0xFF46537A)
                rect(c, bx + bw * panelPos[i], 0f, panelW[i], baseBottom)
            }
//какие этажи рисовать
            val worldBottom = cameraY - (h - viewH)
            val worldTop = cameraY + viewH
            val kFirst = max(0, floor((worldBottom - GROUND_H) / FLOOR_H).toInt() - 1)
            val kLast = ceil((worldTop - GROUND_H) / FLOOR_H).toInt() + 1
            val litLimit = 56 + (f * 70f).toInt()   // чем выше, тем больше горящих окон
            val litColor = lerp(Color(0xFFFFE9A8), Color(0xFFFFD98A), f)
//каждый этаж прекрытие и окна
            for (k in kFirst..kLast) {
                val wb = GROUND_H + k * FLOOR_H

                rect(Color(0xFF0A1024), bx, sy(wb + 5f), bw, 5f)

                for (g in panelPos.indices) {
                    val gapStart = bx + bw * panelPos[g] + panelW[g]
                    val gapEnd = if (g + 1 < panelPos.size) bx + bw * panelPos[g + 1] else bx + bw
                    if (gapEnd - gapStart < 14f) continue

                    var s = (k * 73856093) xor (g * 19349663)
                    s = s xor (s ushr 13)
                    s *= 1274126177
                    s = s xor (s ushr 16)
                    val lit = (s and 0xFF) < litLimit

                    rect(
                        if (lit) litColor else Color(0xFF0F1830),
                        gapStart + 6f, sy(wb + 76f), gapEnd - gapStart - 12f, 62f
                    )
                }
//подцветка панелей и балкон
                for (p in intArrayOf(1, 3)) {
                    rect(Color(0x886D7BA8), bx + bw * panelPos[p], sy(wb + 30f), panelW[p], 24f)
                }

                val balconyX = bx - BALCONY_W
                rect(Color(0xFF2B3654), balconyX, sy(wb + 6f), BALCONY_W, 6f)
                rect(Color(0x558FB2D9), balconyX, sy(wb + 32f), BALCONY_W, 26f)
                rect(Color(0xFFB8D0EA), balconyX, sy(wb + 32f), BALCONY_W, 2f)
                rect(Color(0xFFB8D0EA), balconyX, sy(wb + 32f), 2f, 26f)
            }
//первыйй этаж
            if (sy(GROUND_H + 60f) < h) {
                rect(Color(0xFF0F1830), bx, sy(GROUND_H), bw, GROUND_H)
                val doorY = sy(100f)
                rect(Color(0xFFFFE9A8), w / 2 - 74f, doorY, 70f, 100f)
                rect(Color(0xFFFFE9A8), w / 2 + 4f, doorY, 70f, 100f)
                rect(Color(0xFF0A1024), w / 2 - 4f, doorY, 8f, 100f)
                rect(Color(0xFF8E97B5), bx - 10f, sy(GROUND_H + 8f), bw + 20f, 8f)
            }
//тратуар фонарь и кусты
            if (groundY < h) {
                rect(Color(0xFF2D3142), 0f, groundY, w, h - groundY)
                rect(Color(0xFF454B66), 0f, groundY, w, 4f)
            }
            if (groundY - 230f < h) {
                rect(Color(0xFF8A93AD), w - 16f, sy(200f), 4f, 200f)
                val lamp = Offset((w - 14f) * u, sy(206f) * u)
                drawCircle(Color(0x44FFE9A8), 26f * u, lamp)
                drawCircle(Color(0xFFFFF1B8), 7f * u, lamp)
                drawCircle(Color(0xFF10261F), 22f * u, Offset((bx - 22f) * u, groundY * u))
                drawCircle(Color(0xFF10261F), 15f * u, Offset((bx + 6f) * u, groundY * u))
            }
        }
//вывеска
        if (cameraY < 500f) {
            Image(
                painter = painterResource(R.drawable.ic_sign),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .offset(
                        x = (widthDp / 2 - SIGN_W / 2).dp,
                        y = (viewH - (SIGN_BOTTOM + SIGN_H - cameraY)).dp
                    )
                    .size(width = SIGN_W.dp, height = SIGN_H.dp)
            )
        }
    }
}