package net.galmiza.android.spectrogram.dsp

import kotlin.math.sqrt

/**
 * Turns a frame of samples into a magnitude spectrum: window, FFT, magnitude.
 *
 * Bin k of the result is at k * sampleRate / size Hz; only the size / 2 bins below the
 * Nyquist frequency are returned. Not thread-safe: use one instance per audio thread.
 */
class SpectrumAnalyzer(val size: Int, window: WindowType) {
    private val fft = Fft(size)
    private val coefficients = window.coefficients(size)
    private val re = FloatArray(size)
    private val im = FloatArray(size)

    val binCount get() = size / 2

    /** Writes the magnitude of each bin of [frame] into [out], which holds [binCount] values. */
    fun magnitudes(frame: FloatArray, out: FloatArray) {
        for (i in 0 until size) {
            re[i] = frame[i] * coefficients[i]
            im[i] = 0f
        }
        fft.forward(re, im)
        for (k in 0 until binCount) {
            out[k] = sqrt(re[k] * re[k] + im[k] * im[k])
        }
    }
}
