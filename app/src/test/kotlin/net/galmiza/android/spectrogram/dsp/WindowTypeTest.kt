package net.galmiza.android.spectrogram.dsp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowTypeTest {
    private val n = 1024

    @Test
    fun everyWindowPeaksAtOneInTheMiddle() {
        for (window in WindowType.entries) {
            val c = window.coefficients(n)
            assertEquals("$window", 1f, c[n / 2], 1e-5f)
            assertTrue("$window", c.all { it <= 1f + 1e-5f })
        }
    }

    @Test
    fun everyWindowIsSymmetric() {
        for (window in WindowType.entries) {
            val c = window.coefficients(n)
            for (i in 1 until n) assertEquals("$window[$i]", c[i], c[n - i], 1e-6f)
        }
    }

    @Test
    fun taperedWindowsAreNeverNegativeAndStartNearZero() {
        val tapered = WindowType.entries - WindowType.RECTANGULAR - WindowType.HAMMING
        for (window in tapered) {
            val c = window.coefficients(n)
            // Allow for rounding: Nuttall's first coefficient is exactly 0 on paper
            assertTrue("$window starts at ${c[0]}", c[0] in -1e-7f..0.01f)
            assertTrue("$window goes negative", c.all { it >= -1e-7f })
        }
    }

    @Test
    fun hammingStartsAt8Percent() {
        assertEquals(0.08f, WindowType.HAMMING.coefficients(n)[0], 1e-6f)
    }

    @Test
    fun settingsKeysAreUnchangedSinceVersion1() {
        assertEquals(
            listOf("Rectangular", "Triangular", "Welch", "Hanning", "Hamming", "Blackman", "Nuttall", "Blackman-Nuttall", "Blackman-Harris"),
            WindowType.entries.map { it.key },
        )
        assertEquals(WindowType.BLACKMAN_HARRIS, WindowType.fromKey("Blackman-Harris"))
        assertEquals(WindowType.HAMMING, WindowType.fromKey("unknown"))
        assertEquals(WindowType.HAMMING, WindowType.fromKey(null))
    }
}
