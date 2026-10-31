package net.galmiza.android.spectrogram.dsp

import kotlin.math.PI
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpectrumAnalyzerTest {
    private val sampleRate = 44100
    private val size = 1024
    private val binWidth = sampleRate.toDouble() / size

    private fun tone(frequency: Double) = FloatArray(size) { (0.5 * sin(2 * PI * frequency * it / sampleRate)).toFloat() }

    private fun spectrum(frame: FloatArray, window: WindowType) =
        FloatArray(size / 2).also { SpectrumAnalyzer(size, window).magnitudes(frame, it) }

    @Test
    fun loudestBinIsTheFrequencyOfTheTone() {
        for (frequency in listOf(440.0, 1000.0, 7500.0, 15000.0)) {
            val magnitudes = spectrum(tone(frequency), WindowType.HAMMING)
            val peak = magnitudes.indices.maxBy { magnitudes[it] }
            assertEquals("$frequency Hz", frequency, peak * binWidth, binWidth)
        }
    }

    @Test
    fun blackmanHarrisLeaksFarLessThanRectangular() {
        // A tone halfway between two bins leaks the most energy into distant bins
        val frame = tone(100.5 * binWidth)
        val farBin = 200
        val rectangular = spectrum(frame, WindowType.RECTANGULAR)[farBin]
        val blackmanHarris = spectrum(frame, WindowType.BLACKMAN_HARRIS)[farBin]
        assertTrue("rectangular=$rectangular blackmanHarris=$blackmanHarris", blackmanHarris < rectangular / 100)
    }
}
