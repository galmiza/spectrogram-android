package net.galmiza.android.spectrogram.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * In-place radix-2 FFT for a fixed power-of-two [size].
 *
 * The output is scaled by 1/size, like the original C engine, so a sine of amplitude A
 * shows up with a magnitude of A/2 in its bin.
 */
class Fft(val size: Int) {
    init {
        require(size >= 2 && size and (size - 1) == 0) { "FFT size must be a power of two, got $size" }
    }

    private val cos = FloatArray(size / 2) { cos(2 * PI * it / size).toFloat() }
    private val sin = FloatArray(size / 2) { sin(2 * PI * it / size).toFloat() }
    private val bitReversed = IntArray(size) { Integer.reverse(it) ushr (32 - Integer.numberOfTrailingZeros(size)) }

    /** Replaces ([re], [im]) by their discrete Fourier transform. */
    fun forward(re: FloatArray, im: FloatArray) {
        require(re.size == size && im.size == size) { "Expected arrays of $size samples" }

        for (i in 0 until size) {
            val j = bitReversed[i]
            if (j > i) {
                val tr = re[i]; re[i] = re[j]; re[j] = tr
                val ti = im[i]; im[i] = im[j]; im[j] = ti
            }
        }

        var half = 1
        while (half < size) {
            val twiddleStep = size / (2 * half)
            for (k in 0 until half) {
                val wr = cos[k * twiddleStep]
                val wi = -sin[k * twiddleStep]
                var a = k
                while (a < size) {
                    val b = a + half
                    val tr = wr * re[b] - wi * im[b]
                    val ti = wr * im[b] + wi * re[b]
                    re[b] = re[a] - tr
                    im[b] = im[a] - ti
                    re[a] += tr
                    im[a] += ti
                    a += 2 * half
                }
            }
            half *= 2
        }

        val scale = 1f / size
        for (i in 0 until size) {
            re[i] *= scale
            im[i] *= scale
        }
    }
}
