package net.galmiza.android.spectrogram.audio

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class WavLoopTest {
    @Test
    fun readsTheSamplesAndLoops() {
        val samples = shortArrayOf(1, -2, 3, 32767, -32768)
        val file = File.createTempFile("test", ".wav").apply { deleteOnExit() }
        file.writeBytes(wav(samples))

        val buffer = ShortArray(12)
        WavLoop(file).read(buffer)

        assertArrayEquals(shortArrayOf(1, -2, 3, 32767, -32768, 1, -2, 3, 32767, -32768, 1, -2), buffer)
    }

    /** A minimal 16-bit mono 44.1 kHz WAV file, with an extra chunk before the data like some encoders write. */
    private fun wav(samples: ShortArray): ByteArray {
        val data = samples.size * 2
        return ByteBuffer.allocate(44 + 12 + data).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()); putInt(36 + 12 + data); put("WAVE".toByteArray())
            put("fmt ".toByteArray()); putInt(16); putShort(1); putShort(1); putInt(44100); putInt(88200); putShort(2); putShort(16)
            put("LIST".toByteArray()); putInt(4); put("INFO".toByteArray())
            put("data".toByteArray()); putInt(data); samples.forEach { putShort(it) }
        }.array()
    }
}
