package net.galmiza.android.spectrogram.dsp

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * A synthetic sound to check the display without a microphone: a 200 Hz → 16 kHz sweep
 * repeating every 4 s, a steady 1 kHz tone, a 6 kHz tone pulsed every 0.5 s, and faint noise.
 */
class TestSignal(private val sampleRate: Int) {
    private val random = Random(1)
    private var index = 0L

    /** Fills [buffer] with the next samples. */
    fun read(buffer: ShortArray) {
        for (i in buffer.indices) {
            val t = (index++).toDouble() / sampleRate
            val s = t % SWEEP_SECONDS
            val sweep = 0.25 * sin(2 * PI * (200 * s + (16000 - 200) * s * s / (2 * SWEEP_SECONDS)))
            val steady = 0.05 * sin(2 * PI * 1000 * t)
            val pulsed = if ((t * 2).toInt() % 2 == 0) 0.1 * sin(2 * PI * 6000 * t) else 0.0
            val noise = 0.002 * (random.nextDouble() * 2 - 1)
            buffer[i] = ((sweep + steady + pulsed + noise) * 32767).toInt().toShort()
        }
    }

    private companion object {
        const val SWEEP_SECONDS = 4.0
    }
}
