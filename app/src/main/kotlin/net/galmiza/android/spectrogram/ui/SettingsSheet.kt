package net.galmiza.android.spectrogram.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.galmiza.android.spectrogram.R
import net.galmiza.android.spectrogram.Settings
import net.galmiza.android.spectrogram.dsp.WindowType

/** Settings in a bottom sheet, so changes show live on the spectrogram above. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(settings: Settings, onChange: (Settings) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        SettingsList(settings, onChange)
    }
}

@Composable
fun SettingsList(settings: Settings, onChange: (Settings) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
        Section(R.string.settings_processing)
        Choice(R.string.settings_fft_size, settings.fftSize, Settings.FFT_SIZES, { it.toString() }) {
            onChange(settings.copy(fftSize = it))
        }
        Choice(R.string.settings_window, settings.window, WindowType.entries, { it.key }) {
            onChange(settings.copy(window = it))
        }

        Section(R.string.settings_rendering)
        Choice(R.string.settings_frequency_scale, settings.frequencyScale, FrequencyScale.entries, { stringResource(it.label) }) {
            onChange(settings.copy(frequencyScale = it))
        }
        Choice(R.string.settings_color_scale, settings.colorScale, ColorScale.entries, { it.key }) {
            onChange(settings.copy(colorScale = it))
        }

        Section(R.string.settings_system)
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_keep_screen_on)) },
            supportingContent = { Text(stringResource(R.string.settings_keep_screen_on_summary)) },
            trailingContent = { Switch(checked = settings.keepScreenOn, onCheckedChange = null) },
            modifier = Modifier.clickable { onChange(settings.copy(keepScreenOn = !settings.keepScreenOn)) },
        )

        Section(R.string.settings_about)
        val uriHandler = LocalUriHandler.current
        val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_privacy_policy)) },
            supportingContent = { Text(stringResource(R.string.settings_privacy_policy_summary)) },
            // runCatching: openUri throws if no app can open web links
            modifier = Modifier.clickable { runCatching { uriHandler.openUri(privacyPolicyUrl) } },
        )
    }
}

@Composable
private fun Section(@StringRes title: Int) {
    Text(
        stringResource(title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

/** A setting showing its current value; tapping it opens the list of options. */
@Composable
private fun <T> Choice(@StringRes title: Int, value: T, options: List<T>, label: @Composable (T) -> String, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ListItem(
            headlineContent = { Text(stringResource(title)) },
            supportingContent = { Text(label(value)) },
            modifier = Modifier.clickable { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (option in options) {
                DropdownMenuItem(
                    text = { Text(label(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}
