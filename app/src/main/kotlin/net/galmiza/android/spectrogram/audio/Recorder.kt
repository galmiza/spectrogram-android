package net.galmiza.android.spectrogram.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.concurrent.thread
import net.galmiza.android.spectrogram.dsp.FrameSplitter
import net.galmiza.android.spectrogram.dsp.SpectrumAnalyzer
import net.galmiza.android.spectrogram.dsp.WindowType

/** What the views draw: the latest frame of samples and the spectra computed since the last draw. */
interface AudioFeed {
    /** The latest frame of samples (-1..1), for the waveform. */
    val wave: FloatArray

    /** Spectra computed since the previous call, oldest first. */
    fun takeSpectra(): List<FloatArray>
}

/**
 * Records sound on a background thread and turns it into spectra, one every half frame.
 * With a [testSource] (which fills a buffer with the next samples), analyses that sound instead
 * of the microphone, in real time: for emulators and screenshots.
 */
class Recorder(private val testSource: ((ShortArray) -> Unit)? = null) : AudioFeed {
    @Volatile
    override var wave = FloatArray(0)
        private set

    private val pending = ArrayDeque<FloatArray>()
    @Volatile private var running = false
    private var thread: Thread? = null

    fun start(fftSize: Int, window: WindowType) {
        stop()
        running = true
        thread = thread(name = "Recorder") { record(fftSize, window) }
    }

    fun stop() {
        running = false
        thread?.join()
        thread = null
    }

    override fun takeSpectra(): List<FloatArray> = synchronized(pending) { pending.toList().also { pending.clear() } }

    private fun record(fftSize: Int, window: WindowType) {
        val analyzer = SpectrumAnalyzer(fftSize, window)
        val splitter = FrameSplitter(fftSize) { frame ->
            wave = frame.copyOf()
            val magnitudes = FloatArray(analyzer.binCount).also { analyzer.magnitudes(frame, it) }
            synchronized(pending) {
                pending.addLast(magnitudes)
                if (pending.size > MAX_PENDING) pending.removeFirst()
            }
        }
        val buffer = ShortArray(CHUNK)

        if (testSource != null) {
            while (running) {
                testSource(buffer)
                splitter.push(buffer)
                Thread.sleep(1000L * CHUNK / SAMPLE_RATE)
            }
            return
        }

        val audio = openMicrophone() ?: return
        try {
            audio.startRecording()
            while (running) {
                val count = audio.read(buffer, 0, buffer.size)
                if (count < 0) break
                splitter.push(buffer, count)
            }
        } finally {
            audio.release()
        }
    }

    @SuppressLint("MissingPermission") // The UI starts recording only once RECORD_AUDIO is granted
    private fun openMicrophone(): AudioRecord? {
        val format = AudioFormat.ENCODING_PCM_16BIT
        val channel = AudioFormat.CHANNEL_IN_MONO
        val bufferBytes = maxOf(AudioRecord.getMinBufferSize(SAMPLE_RATE, channel, format), 8 * CHUNK)
        // VOICE_RECOGNITION has no automatic gain control or noise suppression, unlike MIC
        val audio = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, channel, format, bufferBytes)
        if (audio.state == AudioRecord.STATE_INITIALIZED) return audio
        audio.release()
        return null
    }

    companion object {
        const val SAMPLE_RATE = 44100
        private const val CHUNK = 1024 // samples read at a time, 23 ms
        private const val MAX_PENDING = 4096 // spectra kept while nothing draws them
    }
}
