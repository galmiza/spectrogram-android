package net.galmiza.android.spectrogram.dsp

import kotlin.random.Random
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class FrameSplitterTest {
    @Test
    fun framesOverlapByHalf() {
        val frames = mutableListOf<FloatArray>()
        val splitter = FrameSplitter(8) { frames += it.copyOf() }
        splitter.push(ShortArray(20) { (it * 100).toShort() })

        // Frames start every 4 samples: at 0, 4, 8 and 12
        assertEquals(4, frames.size)
        frames.forEachIndexed { f, frame ->
            frame.forEachIndexed { i, value -> assertEquals((4 * f + i) * 100 / 32767f, value, 0f) }
        }
    }

    @Test
    fun chunkSizesDoNotChangeTheFrames() {
        val random = Random(42)
        val samples = ShortArray(5000) { random.nextInt(-32768, 32768).toShort() }

        val whole = mutableListOf<FloatArray>()
        FrameSplitter(256) { whole += it.copyOf() }.push(samples)

        val chunked = mutableListOf<FloatArray>()
        val splitter = FrameSplitter(256) { chunked += it.copyOf() }
        var offset = 0
        while (offset < samples.size) {
            val count = minOf(random.nextInt(1, 700), samples.size - offset)
            splitter.push(samples.copyOfRange(offset, offset + count))
            offset += count
        }

        assertEquals(whole.size, chunked.size)
        whole.indices.forEach { assertArrayEquals(whole[it], chunked[it], 0f) }
    }

    @Test
    fun onlyReadsCountSamples() {
        var frames = 0
        FrameSplitter(4) { frames++ }.push(ShortArray(100), count = 6)
        assertEquals(2, frames)
    }
}
