package net.galmiza.android.spectrogram

import android.content.SharedPreferences
import androidx.core.content.edit
import net.galmiza.android.spectrogram.dsp.WindowType
import net.galmiza.android.spectrogram.ui.ColorScale
import net.galmiza.android.spectrogram.ui.FrequencyScale

/**
 * User settings. They are stored with the same keys and formats as version 1, so settings
 * of users updating from it carry over.
 */
data class Settings(
    val fftSize: Int = DEFAULT_FFT_SIZE,
    val window: WindowType = WindowType.DEFAULT,
    val frequencyScale: FrequencyScale = FrequencyScale.DEFAULT,
    val colorScale: ColorScale = ColorScale.DEFAULT,
    val keepScreenOn: Boolean = true,
) {
    fun save(preferences: SharedPreferences) {
        preferences.edit {
            putString(FFT_SIZE, fftSize.toString())
            putString(WINDOW, window.key)
            putString(FREQUENCY_SCALE, frequencyScale.key)
            putString(COLOR_SCALE, colorScale.key)
            putBoolean(KEEP_SCREEN_ON, keepScreenOn)
        }
    }

    companion object {
        /** 32 to 65536 */
        val FFT_SIZES = List(12) { 32 shl it }
        private const val DEFAULT_FFT_SIZE = 1024

        /** Name of the preferences file used by version 1 (PreferenceManager's default). */
        fun fileName(packageName: String) = "${packageName}_preferences"

        fun load(preferences: SharedPreferences) = Settings(
            fftSize = preferences.getString(FFT_SIZE, null)?.toIntOrNull()?.takeIf { it in FFT_SIZES } ?: DEFAULT_FFT_SIZE,
            window = WindowType.fromKey(preferences.getString(WINDOW, null)),
            frequencyScale = FrequencyScale.fromKey(preferences.getString(FREQUENCY_SCALE, null)),
            colorScale = ColorScale.fromKey(preferences.getString(COLOR_SCALE, null)),
            keepScreenOn = preferences.getBoolean(KEEP_SCREEN_ON, true),
        )

        private const val FFT_SIZE = "fft_resolution"
        private const val WINDOW = "window_type"
        private const val FREQUENCY_SCALE = "frequency_scale"
        private const val COLOR_SCALE = "color_scale"
        private const val KEEP_SCREEN_ON = "keep_screen_on"
    }
}
