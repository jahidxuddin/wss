package de.ju.wss

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import java.awt.Toolkit

class WifiSignalMonitor() {
    fun start(estimator: WifiSignalEstimator) {
        application {
            var signal by remember { mutableStateOf(0F) }
            val signalColor by remember { derivedStateOf { getSignalColor(signal) } }

            LaunchedEffect(Unit) {
                while (true) {
                    val currentSignal = estimator.getSignalStrength() ?: 0F

                    if (currentSignal != signal) {
                        signal = currentSignal
                    }

                    delay(1000L)
                }
            }

            Window(
                onCloseRequest = ::exitApplication,
                title = "Wi-Fi Signal Monitor",
                state = WindowState(
                    width = 150.dp, height = 100.dp, position = WindowPosition.Absolute(
                        (Toolkit.getDefaultToolkit().screenSize.width - 150).dp,
                        (Toolkit.getDefaultToolkit().screenSize.height - 100).dp
                    )
                ),
                icon = painterResource("icon.png"),
                undecorated = true,
                transparent = true,
                alwaysOnTop = true,
                resizable = false,
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 50.dp)) {
                    val barCount = 5
                    val spacing = 8.dp.toPx()
                    val barWidth = 10.dp.toPx()
                    val maxHeight = size.height * 0.6f
                    val strengthLevel = ((signal / 100f) * barCount).toInt().coerceIn(0, barCount)

                    val activeColor = signalColor
                    val inactiveColor = Color.DarkGray

                    for (i in 0 until barCount) {
                        val x = i * (barWidth + spacing)
                        val heightRatio = (i + 1).toFloat() / barCount.toFloat()
                        val barHeight = maxHeight * heightRatio
                        val top = size.height - barHeight

                        drawRect(
                            color = if (i < strengthLevel) activeColor else inactiveColor,
                            topLeft = Offset(x, top),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
            }
        }
    }

    private fun getSignalColor(signal: Float): Color = when {
        signal <= 50 -> Color.Red
        signal <= 90 -> Color.Yellow
        else -> Color.Green
    }
}
