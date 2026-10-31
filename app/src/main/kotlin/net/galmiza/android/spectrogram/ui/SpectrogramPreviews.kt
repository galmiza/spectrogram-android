package net.galmiza.android.spectrogram.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.galmiza.android.spectrogram.dsp.FrameSplitter
import net.galmiza.android.spectrogram.dsp.SpectrumAnalyzer
import net.galmiza.android.spectrogram.dsp.TestSignal
import net.galmiza.android.spectrogram.dsp.WindowType

// Previews that run a synthetic signal through the DSP, to check rendering without a device.

private const val SAMPLE_RATE = 44100

private fun syntheticSignal() = ShortArray(4 * SAMPLE_RATE).also { TestSignal(SAMPLE_RATE).read(it) }

private fun spectrogramImage(samples: ShortArray, scale: ColorScale, window: WindowType, fftSize: Int = 1024, rows: Int = 256): ImageBitmap {
    val analyzer = SpectrumAnalyzer(fftSize, window)
    val magnitudes = FloatArray(analyzer.binCount)
    val columns = mutableListOf<IntArray>()
    FrameSplitter(fftSize) { frame ->
        analyzer.magnitudes(frame, magnitudes)
        columns += IntArray(rows).also { scale.paintColumn(magnitudes, it) }
    }.push(samples)

    val width = columns.size
    val pixels = IntArray(width * rows) { columns[it % width][it / width] }
    return Bitmap.createBitmap(pixels, width, rows, Bitmap.Config.ARGB_8888).asImageBitmap()
}

@Composable
private fun ColumnScope.Panel(label: String, image: ImageBitmap) {
    Text(label, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    Image(
        bitmap = image,
        contentDescription = label,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
    )
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun ColorScalesPreview() {
    val samples = remember { syntheticSignal() }
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        for (scale in ColorScale.entries) {
            Panel("${scale.key} · Hamming · 1024", remember(scale) { spectrogramImage(samples, scale, WindowType.HAMMING) })
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun WindowsPreview() {
    val samples = remember { syntheticSignal() }
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        for (window in listOf(WindowType.RECTANGULAR, WindowType.HAMMING, WindowType.BLACKMAN_HARRIS)) {
            Panel("Fire · ${window.key} · 1024", remember(window) { spectrogramImage(samples, ColorScale.FIRE, window) })
        }
    }
}
