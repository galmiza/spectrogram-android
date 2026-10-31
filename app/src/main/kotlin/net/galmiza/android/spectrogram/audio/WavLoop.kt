package net.galmiza.android.spectrogram.audio

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Plays a WAV file in a loop, as a test sound. The file must be 16-bit mono PCM at 44.1 kHz. */
class WavLoop(file: File) {
    private val samples: ShortArray
    private var position = 0

    init {
        val bytes = file.readBytes()
        val header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        // Skip the RIFF header, then every chunk until "data"
        var offset = 12
        while (String(bytes, offset, 4, Charsets.US_ASCII) != "data") offset += 8 + header.getInt(offset + 4)
        val data = ByteBuffer.wrap(bytes, offset + 8, header.getInt(offset + 4)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        samples = ShortArray(data.remaining()).also { data.get(it) }
    }

    /** Fills [buffer] with the next samples, starting over at the end of the file. */
    fun read(buffer: ShortArray) {
        for (i in buffer.indices) {
            buffer[i] = samples[position]
            position = (position + 1) % samples.size
        }
    }
}
