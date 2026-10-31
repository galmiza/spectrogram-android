# Spectrogram (Android)

Realtime spectrogram app, published on Google Play as `net.galmiza.android.spectrogram`.
It is being rewritten from the 2013–2019 Java + JNI app to Kotlin + Jetpack Compose.

## Rules

- Commit each step on the local `modernize` branch. Never commit to `master`, never push.
- Never commit signing keys, passwords or Play service-account files.
- Keep it simple: one Gradle module, no DI or navigation library unless a real need appears.
- Keep the `applicationId` and keep `versionCode` above 4 (the last Play release).

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

## Legacy reference

The old implementation is on `master` (until the rewrite is merged):
`git show master:app/src/main/java/net/galmiza/android/spectrogram/SpectrogramActivity.java`.
The C DSP code (FFT, window functions) is in https://bitbucket.org/galmiza/sound-engine-android
(`jni/main.c`). Old strings and French translations: `git show master:app/src/main/res/values-fr/strings.xml`.

## Roadmap

1. Skeleton build (done)
2. DSP in Kotlin (`dsp/`: FFT, windows, 50% overlap framing, magnitudes) and color scales, with JVM
   unit tests; matches the C engine except for its window bugs (done). Previews in `ui/SpectrogramPreviews.kt`
   render a synthetic signal through it.
3. App (done): `audio/Recorder` (mic or test signal, background thread), `ui/SpectrogramScreen`
   (top bar, permission flow, lifecycle-aware start/stop), `WaveformView`, `SpectrogramView`
   (scrolling bitmap, one column per spectrum), `SettingsSheet`; `Settings` keeps version 1's keys
4. Screenshot tests, one device smoke test, GitHub Actions (done)
5. Release (prepared): R8 + resource shrinking, signing from `~/.gradle/gradle.properties`,
   adaptive icon, privacy policy (`docs/privacy-policy.html`), store listing in `fastlane/metadata`.
   Steps and Play Console answers: `docs/release.md`. Remaining: upload and Play Console (the user).
