package net.galmiza.android.spectrogram.ui

import androidx.annotation.StringRes
import kotlin.math.ln
import kotlin.math.pow
import net.galmiza.android.spectrogram.R

/**
 * How spectrum bins are spread over the height of the spectrogram. The logarithmic scale
 * starts at bin 1, the lowest non-zero frequency.
 *
 * [key] is the value stored in the settings; it must not change.
 */
enum class FrequencyScale(val key: String, @StringRes val label: Int) {
    LINEAR("Linear", R.string.scale_linear),
    LOGARITHMIC("Logarithmic", R.string.scale_logarithmic);

    /** Bin shown at [position] of the height (0 is the bottom, 1 the top), out of [bins]. */
    fun binAt(position: Double, bins: Int): Double = when (this) {
        LINEAR -> position * bins
        LOGARITHMIC -> bins.toDouble().pow(position)
    }

    /** Position of [bin] on the height; the inverse of [binAt]. */
    fun positionOf(bin: Double, bins: Int): Double = when (this) {
        LINEAR -> bin / bins
        LOGARITHMIC -> ln(bin) / ln(bins.toDouble())
    }

    companion object {
        val DEFAULT = LINEAR

        fun fromKey(key: String?): FrequencyScale = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}
