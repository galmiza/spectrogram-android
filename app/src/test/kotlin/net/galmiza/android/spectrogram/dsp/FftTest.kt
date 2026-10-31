package net.galmiza.android.spectrogram.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test

class FftTest {
    @Test
    fun matchesTheDirectDftDefinition() {
        for (n in listOf(2, 8, 64, 512)) {
            val random = Random(n)
            val signal = FloatArray(n) { random.nextFloat() * 2 - 1 }
            val re = signal.copyOf()
            val im = FloatArray(n)
            Fft(n).forward(re, im)

            for (k in 0 until n) {
                var sumRe = 0.0
                var sumIm = 0.0
                for (t in 0 until n) {
                    val angle = -2 * PI * k * t / n
                    sumRe += signal[t] * cos(angle)
                    sumIm += signal[t] * sin(angle)
                }
                assertEquals("re[$k], n=$n", sumRe / n, re[k].toDouble(), 1e-5)
                assertEquals("im[$k], n=$n", sumIm / n, im[k].toDouble(), 1e-5)
            }
        }
    }

    @Test
    fun sineOfAmplitudeAHasMagnitudeHalfAInItsBin() {
        for (n in listOf(32, 1024, 65536)) {
            val bin = n / 8 + 3
            val re = FloatArray(n) { (0.8 * sin(2 * PI * bin * it / n)).toFloat() }
            val im = FloatArray(n)
            Fft(n).forward(re, im)

            assertEquals("n=$n", 0.4, hypot(re[bin], im[bin]).toDouble(), 1e-4)
            assertEquals("n=$n", 0.0, hypot(re[bin + 1], im[bin + 1]).toDouble(), 1e-4)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsSizesThatAreNotPowersOfTwo() {
        Fft(1000)
    }
}
