package net.galmiza.android.spectrogram

import android.content.SharedPreferences
import net.galmiza.android.spectrogram.dsp.WindowType
import net.galmiza.android.spectrogram.ui.ColorScale
import net.galmiza.android.spectrogram.ui.FrequencyScale
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsTest {
    @Test
    fun readsSettingsSavedByVersion1() {
        // Keys and value formats written by version 1's PreferenceActivity
        val preferences = MapPreferences(
            "fft_resolution" to "4096",
            "window_type" to "Blackman-Harris",
            "frequency_scale" to "Logarithmic",
            "color_scale" to "Ice",
            "keep_screen_on" to false,
            "night_mode" to true, // dropped in version 2: the app follows the system theme
        )
        assertEquals(
            Settings(4096, WindowType.BLACKMAN_HARRIS, FrequencyScale.LOGARITHMIC, ColorScale.ICE, keepScreenOn = false),
            Settings.load(preferences),
        )
    }

    @Test
    fun usesDefaultsWhenNothingIsSavedOrValuesAreUnknown() {
        assertEquals(Settings(), Settings.load(MapPreferences()))
        assertEquals(Settings(), Settings.load(MapPreferences("fft_resolution" to "1000", "window_type" to "Kaiser")))
    }

    @Test
    fun savedSettingsLoadBack() {
        val preferences = MapPreferences()
        val settings = Settings(65536, WindowType.WELCH, FrequencyScale.LOGARITHMIC, ColorScale.GREY, keepScreenOn = false)
        settings.save(preferences)
        assertEquals(settings, Settings.load(preferences))
    }
}

/** In-memory SharedPreferences, enough for Settings. */
private class MapPreferences(vararg initial: Pair<String, Any>) : SharedPreferences {
    private val values = mutableMapOf(*initial)

    override fun getAll(): Map<String, *> = values
    override fun getString(key: String, defValue: String?) = values[key] as String? ?: defValue
    override fun getStringSet(key: String, defValues: Set<String>?) = defValues
    override fun getInt(key: String, defValue: Int) = values[key] as Int? ?: defValue
    override fun getLong(key: String, defValue: Long) = values[key] as Long? ?: defValue
    override fun getFloat(key: String, defValue: Float) = values[key] as Float? ?: defValue
    override fun getBoolean(key: String, defValue: Boolean) = values[key] as Boolean? ?: defValue
    override fun contains(key: String) = key in values
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        override fun putString(key: String, value: String?) = apply { values[key] = value!! }
        override fun putStringSet(key: String, values: Set<String>?) = this
        override fun putInt(key: String, value: Int) = apply { values[key] = value }
        override fun putLong(key: String, value: Long) = apply { values[key] = value }
        override fun putFloat(key: String, value: Float) = apply { values[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { values[key] = value }
        override fun remove(key: String) = apply { values.remove(key) }
        override fun clear() = apply { values.clear() }
        override fun commit() = true
        override fun apply() = Unit
    }
}
