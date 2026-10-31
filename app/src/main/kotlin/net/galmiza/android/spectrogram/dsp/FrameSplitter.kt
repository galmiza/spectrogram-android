package net.galmiza.android.spectrogram.dsp

/**
 * Cuts a continuous stream of 16-bit samples into frames of [size] samples that overlap
 * by half, so every sample is analysed twice. Samples are scaled to -1..1.
 *
 * [onFrame] receives an internal buffer that is reused for the next frame: read it, don't
 * keep it.
 */
class FrameSplitter(val size: Int, private val onFrame: (FloatArray) -> Unit) {
    init {
        require(size >= 2 && size % 2 == 0) { "Frame size must be even, got $size" }
    }

    private val frame = FloatArray(size)
    private var filled = 0

    fun push(samples: ShortArray, count: Int = samples.size) {
        for (i in 0 until count) {
            frame[filled++] = samples[i] / 32767f
            if (filled == size) {
                onFrame(frame)
                frame.copyInto(frame, destinationOffset = 0, startIndex = size / 2, endIndex = size)
                filled = size / 2
            }
        }
    }
}
