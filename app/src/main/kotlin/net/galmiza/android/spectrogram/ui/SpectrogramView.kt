package net.galmiza.android.spectrogram.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import net.galmiza.android.spectrogram.Settings
import net.galmiza.android.spectrogram.audio.AudioFeed
import net.galmiza.android.spectrogram.audio.Recorder

/**
 * Scrolling spectrogram: time goes left to right, frequency bottom to top. The palette is
 * shown on the left and frequencies on the right.
 */
@Composable
fun SpectrogramView(feed: AudioFeed, settings: Settings, running: Boolean, modifier: Modifier = Modifier) {
    val frameTime = rememberFrameTime(running)
    val image = remember { ScrollingImage() }
    val textMeasurer = rememberTextMeasurer(cacheSize = 64) // up to 22 labels, laid out once
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = Color.White)
    val palette = remember(settings.colorScale) { Brush.verticalGradient(settings.colorScale.colors.map { Color(it) }) }

    Canvas(modifier.background(Color.Black).clipToBounds()) {
        frameTime.longValue // redraw on every frame while running
        val paletteWidth = 6.dp.toPx()
        val labelsWidth = 32.dp.toPx()
        image.resize((size.width - paletteWidth - labelsWidth).toInt(), size.height.toInt())
        for (magnitudes in feed.takeSpectra()) image.add(magnitudes, settings)

        drawRect(palette, size = Size(paletteWidth, size.height))
        translate(left = paletteWidth) { image.draw(this) }
        drawFrequencyLabels(textMeasurer, labelStyle, settings, left = size.width - labelsWidth + 4.dp.toPx())
    }
}

private fun DrawScope.drawFrequencyLabels(textMeasurer: TextMeasurer, style: TextStyle, settings: Settings, left: Float) {
    val bins = settings.fftSize / 2
    val binHz = Recorder.SAMPLE_RATE.toDouble() / settings.fftSize
    val nyquist = Recorder.SAMPLE_RATE / 2
    val frequencies = when (settings.frequencyScale) {
        FrequencyScale.LINEAR -> {
            val step = listOf(1000, 2000, 5000).firstOrNull { size.height * it / nyquist >= 20.dp.toPx() } ?: 10000
            (step..nyquist - step / 2 step step).toList()
        }
        FrequencyScale.LOGARITHMIC -> listOf(10, 100, 1000, 10000).filter { it > binHz }
    }
    for (hz in frequencies) {
        val y = size.height * (1 - settings.frequencyScale.positionOf(hz / binHz, bins)).toFloat()
        val text = textMeasurer.measure(if (hz >= 1000) "${hz / 1000}k" else "$hz", style)
        val top = (y - text.size.height / 2f).coerceIn(0f, size.height - text.size.height)
        drawText(text, topLeft = Offset(left, top))
    }
}

/** A bitmap where each spectrum becomes a column; once it is full, older columns scroll out to the left. */
private class ScrollingImage {
    private var bitmap: Bitmap? = null
    private var image: ImageBitmap? = null
    private var column = IntArray(0)
    private var next = 0 // where the next column goes
    private var full = false

    fun resize(width: Int, height: Int) {
        if (bitmap?.width == width && bitmap?.height == height) return
        if (width <= 0 || height <= 0) {
            bitmap = null
            image = null
            return
        }
        bitmap = createBitmap(width, height).also {
            it.eraseColor(android.graphics.Color.BLACK)
            image = it.asImageBitmap()
        }
        column = IntArray(height)
        next = 0
        full = false
    }

    fun add(magnitudes: FloatArray, settings: Settings) {
        val bitmap = bitmap ?: return
        settings.colorScale.paintColumn(magnitudes, column, settings.frequencyScale)
        bitmap.setPixels(column, 0, 1, next, 0, 1, bitmap.height)
        next = (next + 1) % bitmap.width
        if (next == 0) full = true
    }

    fun draw(scope: DrawScope) {
        val image = image ?: return
        if (!full) {
            scope.drawImage(image)
            return
        }
        // The oldest column is at `next`: draw from there to the end, then the start after it
        val height = image.height
        val older = image.width - next
        scope.drawImage(image, srcOffset = IntOffset(next, 0), srcSize = IntSize(older, height), dstSize = IntSize(older, height))
        if (next > 0) {
            scope.drawImage(image, srcSize = IntSize(next, height), dstOffset = IntOffset(older, 0), dstSize = IntSize(next, height))
        }
    }
}
