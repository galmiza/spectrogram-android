package net.galmiza.android.spectrogram

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import java.io.File
import net.galmiza.android.spectrogram.audio.Recorder
import net.galmiza.android.spectrogram.audio.WavLoop
import net.galmiza.android.spectrogram.dsp.TestSignal
import net.galmiza.android.spectrogram.ui.SpectrogramScreen
import net.galmiza.android.spectrogram.ui.theme.SpectrogramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Debug builds can analyse a test sound instead of the microphone, for emulators and screenshots:
        // adb shell am start -n <package>/net.galmiza.android.spectrogram.MainActivity
        //   --ez test_signal true       built-in sweep and tones
        //   --es test_audio <path.wav>  a 16-bit mono 44.1 kHz WAV file, played in a loop
        val debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val testAudio = intent.getStringExtra("test_audio")
        val testSource = when {
            !debuggable -> null
            testAudio != null -> WavLoop(File(testAudio))::read
            intent.getBooleanExtra("test_signal", false) -> TestSignal(Recorder.SAMPLE_RATE)::read
            else -> null
        }
        val recorder = Recorder(testSource)
        val preferences = getSharedPreferences(Settings.fileName(packageName), MODE_PRIVATE)

        setContent {
            SpectrogramTheme {
                SpectrogramScreen(recorder, preferences)
            }
        }
    }
}
