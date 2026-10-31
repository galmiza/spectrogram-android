# Spectrogram (Android)

Realtime spectrogram app in Kotlin and Jetpack Compose, published on Google Play as
`net.galmiza.android.spectrogram`.

## Rules

- Work on a local branch and commit each step there. The owner commits to `master` and pushes.
- Commit messages are one-line Conventional Commits: `feat(ui): …`, `fix(dsp): …`, `docs: …`.
- Never commit signing keys, passwords or Play service-account files.
- Keep it simple: one Gradle module, no DI or navigation library unless a real need appears.
- Never change the `applicationId`; increase `versionCode` for every Play upload.
- Never change the settings keys or value formats: installed apps already have settings saved with them.

## Stack

Kotlin, Jetpack Compose + Material 3, AGP 9.5, Gradle 9.8, compileSdk/targetSdk 37,
minSdk 26. Versions live in `gradle/libs.versions.toml`. Gradle downloads JDK 17 by
itself (`gradle/gradle-daemon-jvm.properties`). Tool setup: `docs/dev-setup.md`.

## Commands

```sh
./gradlew assembleDebug                      # build
./gradlew lintDebug                          # Android Lint
android emulator start --headless medium_phone
android run app                              # build, install and launch on the running emulator
adb shell pm grant net.galmiza.android.spectrogram.debug android.permission.RECORD_AUDIO   # skip the permission screen
adb shell am start -S -n net.galmiza.android.spectrogram.debug/net.galmiza.android.spectrogram.MainActivity \
    --ez test_signal true                    # debug build: analyse a test signal instead of the mic
    # or --es test_audio <path to a 16-bit mono 44.1 kHz WAV readable by the app>, played in a loop
android screen capture -o /tmp/screen.png    # screenshot of the device
android layout --no-idle                     # UI tree as JSON (--no-idle: the app redraws every frame)
android preview list app                     # Compose @Preview functions
android preview render app --fqn=<name from list> -o /tmp/previews   # render without a device
android emulator stop medium_phone
./gradlew bundleRelease                      # Play bundle; signed only if the key is configured (docs/release.md)
```

After a UI change, check it with `android preview render` (fast, no device) or
`android run app` + `android screen capture`.

The emulator has audio turned off (it crashes on this Mac otherwise, see `docs/dev-setup.md`):
use the test signal there and a real phone for microphone checks.

## Tests

```sh
./gradlew testDebugUnitTest                           # JVM unit tests (DSP, scales, settings), seconds
./gradlew testDebugScreenshotTestDefaultTestSuite     # compare screens with the reference images
./gradlew updateDebugScreenshotTestDefaultTestSuite   # re-record them after an intended UI change
./gradlew phoneDebugAndroidTest                       # smoke test on an emulator Gradle starts itself
./gradlew connectedDebugAndroidTest                   # same smoke test on the running emulator or phone
```

- Screenshot tests are the `@PreviewTest` functions in `app/src/screenshotTest`; reference images are
  in `app/src/screenshotTestDefaultDebug/reference` (committed). They render `SpectrogramContent`
  with a fake `AudioFeed` fed by the test signal. Look at the new images before committing them.
- The smoke test (`app/src/androidTest`) uses UiAutomator, not Compose test APIs: the live
  spectrogram redraws every frame, so Compose never becomes idle.
- CI (`.github/workflows/ci.yml`) runs all of them; screenshot tests run on macOS, where the
  references are recorded.

## Code

All in `app/src/main/kotlin/net/galmiza/android/spectrogram/`:

- `dsp/`: FFT, window functions, 50% overlap framing, magnitudes, and the test signal. Plain
  Kotlin, unit tested on the JVM.
- `audio/Recorder`: reads the microphone (or, in debug builds, a test source such as `WavLoop`)
  on a background thread and turns it into spectra.
- `ui/SpectrogramScreen`: top bar, permission flow, recording that follows the screen's lifecycle.
  `WaveformView`, `SpectrogramView` (scrolling bitmap, one column per spectrum), `SettingsSheet`,
  color and frequency scales. `SpectrogramPreviews` renders the test signal for `@Preview`.
- `Settings`: user settings in SharedPreferences.

Releasing (signing, store listing in `fastlane/metadata`, privacy policy `docs/privacy-policy.html`
served by GitHub Pages, Play Console answers): `docs/release.md`.
