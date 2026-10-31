package net.galmiza.android.spectrogram.ui

import kotlin.math.log10
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Palettes that turn a spectrum magnitude into an ARGB color, as in version 1: the magnitude
 * is converted to decibels below full scale, and 0 to 111 dB is spread along the palette,
 * from its first color (loud) to its last (silent).
 *
 * [key] is the value stored in the settings; it must not change.
 */
enum class ColorScale(val key: String, val colors: IntArray) {
    FIRE("Fire", argb(0xFFFFFFFF, 0xFFFFFF00, 0xFFFF0000, 0xFF000000)),
    ICE("Ice", argb(0xFFFFFFFF, 0xFF00FFFF, 0xFF0000FF, 0xFF000000)),
    GREY("Grey", argb(0xFFFFFFFF, 0xFF000000)),
    RAINBOW("Rainbow", argb(0xFFFFFFFF, 0xFFFF00FF, 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFF000000));

    /** Color at [position] along the palette: 0 is the first color, 1 the last. */
    fun colorAt(position: Float): Int {
        if (!(position > 0f)) return colors.first()
        if (position >= 1f) return colors.last()
        val scaled = position * (colors.size - 1)
        val i = scaled.toInt()
        return blend(colors[i], colors[i + 1], scaled - i)
    }

    fun colorOf(magnitude: Float): Int = colorAt(max(0f, -20f * log10(magnitude)) * POSITION_PER_DB)

    /**
     * Paints one spectrogram column: [pixels] holds one color per screen row, top row first,
     * with low frequencies at the bottom. When several bins share a row, the loudest wins so
     * that narrow tones stay visible at large FFT sizes.
     */
    fun paintColumn(magnitudes: FloatArray, pixels: IntArray, frequencyScale: FrequencyScale = FrequencyScale.LINEAR) {
        val rows = pixels.size
        val bins = magnitudes.size
        for (row in 0 until rows) {
            val fromBottom = rows - 1 - row
            val first = frequencyScale.binAt(fromBottom.toDouble() / rows, bins).toInt().coerceIn(0, bins - 1)
            val last = frequencyScale.binAt((fromBottom + 1).toDouble() / rows, bins).toInt().coerceIn(first + 1, bins)
            var loudest = 0f
            for (bin in first until last) loudest = max(loudest, magnitudes[bin])
            pixels[row] = colorOf(loudest)
        }
    }

    companion object {
        val DEFAULT = FIRE
        const val POSITION_PER_DB = 0.009f

        fun fromKey(key: String?): ColorScale = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

private fun argb(vararg colors: Long) = IntArray(colors.size) { colors[it].toInt() }

private fun blend(from: Int, to: Int, fraction: Float): Int {
    fun channel(shift: Int): Int {
        val a = (from ushr shift) and 0xFF
        val b = (to ushr shift) and 0xFF
        return (a + (fraction * (b - a)).roundToInt()) shl shift
    }
    return channel(24) or channel(16) or channel(8) or channel(0)
}
