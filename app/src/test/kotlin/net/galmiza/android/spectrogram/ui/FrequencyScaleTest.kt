package net.galmiza.android.spectrogram.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FrequencyScaleTest {
    @Test
    fun linearSpreadsBinsEvenly() {
        assertEquals(0.0, FrequencyScale.LINEAR.binAt(0.0, 512), 0.0)
        assertEquals(256.0, FrequencyScale.LINEAR.binAt(0.5, 512), 0.0)
        assertEquals(512.0, FrequencyScale.LINEAR.binAt(1.0, 512), 0.0)
    }

    @Test
    fun logarithmicGoesFromTheFirstBinToTheLast() {
        assertEquals(1.0, FrequencyScale.LOGARITHMIC.binAt(0.0, 512), 1e-9)
        assertEquals(512.0, FrequencyScale.LOGARITHMIC.binAt(1.0, 512), 1e-9)
        // Each octave gets the same height: 9 octaves from bin 1 to 512
        assertEquals(2.0, FrequencyScale.LOGARITHMIC.binAt(1.0 / 9, 512), 1e-9)
    }

    @Test
    fun positionOfIsTheInverseOfBinAt() {
        for (scale in FrequencyScale.entries) {
            for (position in listOf(0.1, 0.37, 0.8)) {
                assertEquals("$scale", position, scale.positionOf(scale.binAt(position, 1024), 1024), 1e-9)
            }
        }
    }

    @Test
    fun logarithmicColumnGivesLowFrequenciesMoreRows() {
        val magnitudes = FloatArray(1024).also { it[2] = 1f }
        val pixels = IntArray(10)
        ColorScale.GREY.paintColumn(magnitudes, pixels, FrequencyScale.LOGARITHMIC)
        // Row k from the bottom covers bins 2^k until 2^(k+1): bin 2 is in the 2nd row from the bottom
        val black = 0xFF000000.toInt()
        assertEquals(List(8) { black } + 0xFFFFFFFF.toInt() + black, pixels.toList())
    }

    @Test
    fun settingsKeysAreUnchangedSinceVersion1() {
        assertEquals(listOf("Linear", "Logarithmic"), FrequencyScale.entries.map { it.key })
        assertEquals(FrequencyScale.LINEAR, FrequencyScale.fromKey(null))
    }
}
