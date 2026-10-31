package net.galmiza.android.spectrogram.ui

import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.LongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import net.galmiza.android.spectrogram.R
import net.galmiza.android.spectrogram.Settings
import net.galmiza.android.spectrogram.audio.AudioFeed
import net.galmiza.android.spectrogram.audio.Recorder

/** The app's screen: holds the state, the recording and the settings. */
@Composable
fun SpectrogramScreen(recorder: Recorder, preferences: SharedPreferences) {
    var settings by remember { mutableStateOf(Settings.load(preferences)) }
    var running by rememberSaveable { mutableStateOf(true) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val (microphoneAllowed, requestMicrophone) = rememberMicrophonePermission()

    // Record only while the app is visible, running, and allowed to
    LifecycleStartEffect(microphoneAllowed, running, settings.fftSize, settings.window) {
        if (microphoneAllowed && running) recorder.start(settings.fftSize, settings.window)
        onStopOrDispose { recorder.stop() }
    }
    KeepScreenOn(settings.keepScreenOn && running)

    SpectrogramContent(
        feed = recorder,
        settings = settings,
        running = running,
        microphoneAllowed = microphoneAllowed,
        onToggleRunning = { running = !running },
        onRequestMicrophone = requestMicrophone,
        onOpenSettings = { showSettings = true },
    )
    if (showSettings) {
        SettingsSheet(
            settings = settings,
            onChange = {
                settings = it
                it.save(preferences)
            },
            onDismiss = { showSettings = false },
        )
    }
}

/** What the screen shows, given its state; used directly by screenshot tests. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpectrogramContent(
    feed: AudioFeed,
    settings: Settings,
    running: Boolean,
    microphoneAllowed: Boolean,
    onToggleRunning: () -> Unit,
    onRequestMicrophone: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var showWave by rememberSaveable { mutableStateOf(true) }
    var showSpectrogram by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name))
                        Text(stringResource(R.string.app_subtitle), style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    if (microphoneAllowed) {
                        IconButton(onClick = onToggleRunning) {
                            Icon(
                                painterResource(if (running) R.drawable.ic_pause else R.drawable.ic_play),
                                contentDescription = stringResource(if (running) R.string.action_pause else R.string.action_resume),
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(painterResource(R.drawable.ic_settings), contentDescription = stringResource(R.string.action_settings))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (!microphoneAllowed) {
                PermissionMessage(onAllow = onRequestMicrophone)
                return@Column
            }
            val frameMs = "%.2f".format(1000.0 * settings.fftSize / Recorder.SAMPLE_RATE)
            Header(stringResource(R.string.header_time, frameMs)) { showWave = !showWave }
            if (showWave) {
                WaveformView(feed, running, Modifier.fillMaxWidth().weight(1f))
            }
            Header(stringResource(R.string.header_frequency, settings.fftSize, settings.window.key)) { showSpectrogram = !showSpectrogram }
            if (showSpectrogram) {
                SpectrogramView(feed, settings, running, Modifier.fillMaxWidth().weight(2f))
            }
        }
    }
}

/** Tapping a header shows or hides the view below it. */
@Composable
private fun Header(text: String, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    )
}

@Composable
private fun PermissionMessage(onAllow: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.permission_explanation), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAllow) { Text(stringResource(R.string.permission_button)) }
    }
}

/**
 * Whether the microphone may be used, and a function to ask for it. Asks once on start;
 * once the system stops showing its dialog, asking opens the app's system settings instead.
 */
@Composable
private fun rememberMicrophonePermission(): Pair<Boolean, () -> Unit> {
    val activity = LocalActivity.current ?: return false to {}
    fun isGranted() = activity.checkSelfPermission(RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    var granted by remember { mutableStateOf(isGranted()) }
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        asked = true
    }
    LaunchedEffect(Unit) {
        if (!granted && !asked) launcher.launch(RECORD_AUDIO)
    }
    // The user may have changed it in the system settings
    LifecycleResumeEffect(Unit) {
        granted = isGranted()
        onPauseOrDispose {}
    }
    return granted to {
        if (!asked || activity.shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
            launcher.launch(RECORD_AUDIO)
        } else {
            activity.startActivity(Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null)))
        }
    }
}

@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

/** Changes on every display frame while [running], so that drawing code reading it redraws continuously. */
@Composable
internal fun rememberFrameTime(running: Boolean): LongState {
    val time = remember { mutableLongStateOf(0L) }
    LaunchedEffect(running) {
        while (running) withFrameNanos { time.longValue = it }
    }
    return time
}
