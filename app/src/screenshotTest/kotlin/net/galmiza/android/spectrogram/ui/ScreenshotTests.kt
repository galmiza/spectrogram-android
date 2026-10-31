package net.galmiza.android.spectrogram.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import net.galmiza.android.spectrogram.Settings
import net.galmiza.android.spectrogram.audio.AudioFeed
import net.galmiza.android.spectrogram.audio.Recorder
import net.galmiza.android.spectrogram.dsp.FrameSplitter
import net.galmiza.android.spectrogram.dsp.SpectrumAnalyzer
import net.galmiza.android.spectrogram.dsp.TestSignal
import net.galmiza.android.spectrogram.ui.theme.SpectrogramTheme

/** 12 s of the test signal, analysed like the recorder does: enough columns to fill the screen. */
private class TestSignalFeed(settings: Settings) : AudioFeed {
    override var wave = FloatArray(0)
    private val spectra = mutableListOf<FloatArray>()

    init {
        val analyzer = SpectrumAnalyzer(settings.fftSize, settings.window)
        val samples = ShortArray(12 * Recorder.SAMPLE_RATE).also { TestSignal(Recorder.SAMPLE_RATE).read(it) }
        FrameSplitter(settings.fftSize) { frame ->
            wave = frame.copyOf()
            spectra += FloatArray(analyzer.binCount).also { analyzer.magnitudes(frame, it) }
        }.push(samples)
    }

    override fun takeSpectra() = spectra.toList().also { spectra.clear() }
}

// Phone sizes at a low density, to keep the reference images small
private const val PHONE = "spec:width=411dp,height=891dp,dpi=240"
private const val PHONE_LANDSCAPE = "spec:width=891dp,height=411dp,dpi=240"

@Composable
private fun Screen(settings: Settings = Settings(), microphoneAllowed: Boolean = true) {
    SpectrogramTheme {
        SpectrogramContent(
            feed = remember { TestSignalFeed(settings) },
            settings = settings,
            running = true,
            microphoneAllowed = microphoneAllowed,
            onToggleRunning = {},
            onRequestMicrophone = {},
            onOpenSettings = {},
        )
    }
}

@PreviewTest
@Preview(device = PHONE)
@Composable
fun Live() = Screen()

@PreviewTest
@Preview(device = PHONE, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DarkLogarithmicRainbow() = Screen(Settings(frequencyScale = FrequencyScale.LOGARITHMIC, colorScale = ColorScale.RAINBOW))

@PreviewTest
@Preview(device = PHONE_LANDSCAPE)
@Composable
fun Landscape() = Screen()

@PreviewTest
@Preview(device = PHONE, locale = "fr")
@Composable
fun French() = Screen(Settings(fftSize = 4096))

@PreviewTest
@Preview(device = PHONE)
@Composable
fun MicrophoneNotAllowed() = Screen(microphoneAllowed = false)

@PreviewTest
@Preview(showBackground = true, heightDp = 560)
@Composable
fun SettingsSheetContent() = SpectrogramTheme { SettingsList(Settings(), onChange = {}) }
