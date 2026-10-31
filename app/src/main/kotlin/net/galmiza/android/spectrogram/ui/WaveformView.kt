package net.galmiza.android.spectrogram.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import net.galmiza.android.spectrogram.audio.AudioFeed

/** The latest frame of samples. Drag up or down to change the amplitude zoom. */
@Composable
fun WaveformView(feed: AudioFeed, running: Boolean, modifier: Modifier = Modifier) {
    val frameTime = rememberFrameTime(running)
    var gain by remember { mutableFloatStateOf(1f) }
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val waveColor = MaterialTheme.colorScheme.onBackground
    val paint = remember { Paint() }
    var points = remember { FloatArray(0) }

    Canvas(
        modifier
            .clipToBounds()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount -> gain = (gain * (1 - dragAmount * 0.01f)).coerceIn(0.1f, 100f) }
            }
    ) {
        frameTime.longValue // redraw on every frame while running
        val middle = size.height / 2
        drawLine(axisColor, Offset(0f, middle), Offset(size.width, middle))

        val wave = feed.wave
        val columns = size.width.toInt()
        if (wave.isEmpty() || columns == 0) return@Canvas
        // Two points per pixel column, drawn as connected line segments (much cheaper than a stroked path)
        if (points.size != 4 * columns) points = FloatArray(4 * columns)
        for (x in 0 until columns) {
            // Each pixel column spans the range of the samples that fall into it
            val from = x * wave.size / columns
            val to = max(from + 1, (x + 1) * wave.size / columns)
            var low = wave[from]
            var high = low
            for (i in from until to) {
                low = min(low, wave[i])
                high = max(high, wave[i])
            }
            points[4 * x] = x.toFloat()
            points[4 * x + 1] = middle * (1 - high * gain)
            points[4 * x + 2] = x.toFloat()
            points[4 * x + 3] = middle * (1 - low * gain)
        }
        paint.color = waveColor
        paint.strokeWidth = 1.dp.toPx()
        drawIntoCanvas { it.drawRawPoints(PointMode.Polygon, points, paint) }
    }
}
