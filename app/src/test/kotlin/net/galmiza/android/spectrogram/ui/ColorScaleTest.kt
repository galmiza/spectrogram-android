package net.galmiza.android.spectrogram.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ColorScaleTest {
    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()

    @Test
    fun fullScaleIsTheFirstColorAndSilenceTheLast() {
        for (scale in ColorScale.entries) {
            assertEquals("$scale", white, scale.colorOf(1f))
            assertEquals("$scale", black, scale.colorOf(0f))
            assertEquals("$scale", black, scale.colorOf(1e-6f)) // -120 dB
        }
    }

    @Test
    fun blendsBetweenNeighbouringColors() {
        assertEquals(0xFF808080.toInt(), ColorScale.GREY.colorAt(0.5f))
        // Fire: white -> yellow -> red -> black; one third of the way is pure yellow
        assertEquals(0xFFFFFF00.toInt(), ColorScale.FIRE.colorAt(1f / 3))
        // -55.6 dB is the middle of the 0..111 dB range
        assertEquals(ColorScale.GREY.colorAt(0.5f), ColorScale.GREY.colorOf(1.6681e-3f))
    }

    @Test
    fun paintColumnPutsLowFrequenciesAtTheBottom() {
        val magnitudes = FloatArray(8).also { it[0] = 1f }
        val pixels = IntArray(8)
        ColorScale.GREY.paintColumn(magnitudes, pixels)
        assertEquals(List(7) { black } + white, pixels.toList())
    }

    @Test
    fun paintColumnKeepsTheLoudestBinWhenBinsShareARow() {
        val magnitudes = FloatArray(1024).also { it[500] = 1f }
        val pixels = IntArray(10)
        ColorScale.GREY.paintColumn(magnitudes, pixels)
        // Bin 500 of 1024 is in the 5th row from the bottom
        assertEquals(List(5) { black } + white + List(4) { black }, pixels.toList())
    }

    @Test
    fun settingsKeysAreUnchangedSinceVersion1() {
        assertEquals(listOf("Fire", "Ice", "Grey", "Rainbow"), ColorScale.entries.map { it.key })
        assertEquals(ColorScale.FIRE, ColorScale.fromKey("bogus"))
    }
}
