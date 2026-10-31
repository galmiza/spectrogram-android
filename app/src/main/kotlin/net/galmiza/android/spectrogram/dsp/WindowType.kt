package net.galmiza.android.spectrogram.dsp

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

/**
 * Window functions applied to each frame before the FFT to reduce spectral leakage.
 *
 * [key] is the value stored in the settings; it must not change, so that settings saved
 * by earlier versions keep working. Coefficients are periodic (DFT-even), as usual for
 * spectral analysis, and peak at 1 in the middle of the frame.
 *
 * Version 1 (C engine) computed the Blackman, Nuttall, Blackman-Nuttall and Blackman-Harris
 * windows with wrong phases for their higher cosine terms (negative at the edges, peaking at
 * 0.83–0.92) and had small off-by-one errors in Triangular and Welch; these are the textbook
 * formulas.
 */
enum class WindowType(val key: String) {
    RECTANGULAR("Rectangular"),
    TRIANGULAR("Triangular"),
    WELCH("Welch"),
    HANN("Hanning"),
    HAMMING("Hamming"),
    BLACKMAN("Blackman"),
    NUTTALL("Nuttall"),
    BLACKMAN_NUTTALL("Blackman-Nuttall"),
    BLACKMAN_HARRIS("Blackman-Harris");

    fun coefficients(size: Int): FloatArray = FloatArray(size) { value(it, size).toFloat() }

    private fun value(i: Int, n: Int): Double {
        val x = 2 * PI * i / n
        return when (this) {
            RECTANGULAR -> 1.0
            TRIANGULAR -> 1 - abs(2.0 * i / n - 1)
            WELCH -> (2.0 * i / n - 1).let { 1 - it * it }
            HANN -> cosineSum(x, 0.5, 0.5)
            HAMMING -> cosineSum(x, 0.54, 0.46)
            BLACKMAN -> cosineSum(x, 0.42659, 0.49656, 0.076849)
            NUTTALL -> cosineSum(x, 0.355768, 0.487396, 0.144232, 0.012604)
            BLACKMAN_NUTTALL -> cosineSum(x, 0.3635819, 0.4891775, 0.1365995, 0.0106411)
            BLACKMAN_HARRIS -> cosineSum(x, 0.35875, 0.48829, 0.14128, 0.01168)
        }
    }

    companion object {
        val DEFAULT = HAMMING

        fun fromKey(key: String?): WindowType = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

/** a0 - a1·cos(x) + a2·cos(2x) - a3·cos(3x) … */
private fun cosineSum(x: Double, vararg a: Double): Double =
    a.indices.sumOf { k -> (if (k % 2 == 0) 1 else -1) * a[k] * cos(k * x) }
