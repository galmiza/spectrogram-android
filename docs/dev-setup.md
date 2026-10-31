# Development setup

What to install to build and test this app from the command line, and why each
piece is there. Written for macOS on Apple Silicon, October 2026.

> This page covers the tools. Build, run and test commands are in `CLAUDE.md`.

## What to install

| Tool | Version | Why |
|---|---|---|
| [Homebrew](https://brew.sh) | – | Installs and updates the command-line tools below. |
| `android-commandlinetools` (Homebrew cask) | 22.0 (`android` 1.1) | Provides `android`, Google's Android CLI (2026). It installs SDK packages, creates and starts emulators, takes screenshots and dumps the UI tree. It replaces `sdkmanager` and `avdmanager`, which now print a deprecation warning, and it is built to be driven by coding agents such as Claude Code. |
| Android SDK in `~/Library/Android/sdk` | – | The default location used by the `android` CLI and by Android Studio. |
| ↳ `platforms/android-37.0` | Android 17 | The app compiles against and targets API 37. Google Play has required API 36 or higher for app updates since 31 Aug 2026. |
| ↳ `build-tools/37.0.0` | 37.0.0 | `aapt2`, `zipalign`, `apksigner`, used by the build and for checking signatures. |
| ↳ `platform-tools` | 37.0.1 | `adb`, which talks to emulators and phones. |
| ↳ `emulator` | 37.2.13 | Runs virtual devices. Apple Silicon runs arm64 images natively, so it is fast. |
| ↳ `system-images/android-36/google_apis_playstore/arm64-v8a` | Android 16 | The image the `medium_phone` profile uses. It includes the Play Store, so it behaves like a real phone. |
| JDK | Homebrew `openjdk` (any version ≥ 17) | Only used to start the Gradle wrapper. The build itself runs on JDK 17, which Gradle downloads automatically into `~/.gradle/jdks` (pinned in `gradle/gradle-daemon-jvm.properties`), so every machine and CI use the same JDK. |
| Android Studio | optional | Not needed to build or test. Install it if you want a visual debugger, layout inspector or profiler. |

## One-time setup

```sh
brew install --cask android-commandlinetools   # provides `android`
android update                                  # update the CLI itself
android init                                    # install the android-cli skill for coding agents
android sdk install platforms/android-37.0 build-tools/37.0.0 platform-tools emulator
android emulator create medium_phone            # downloads the Android 16 image (about 1.5 GB)
```

Then add the SDK location to `~/.zshrc`, so Gradle finds it without a
`local.properties` file:

```sh
export ANDROID_HOME="$HOME/Library/Android/sdk"
```

`android init` installs Google's `android-cli` skill for every coding agent it
finds on the machine (Claude Code, Codex, Copilot…). Run `android skills list`
for the other official skills (for example `play-policy-insights`); none of them
is required for this project.

## Daily use

```sh
android emulator start medium_phone            # returns once booted; add --headless for no window
adb devices                                    # should list emulator-5554
android screen capture -o screen.png           # screenshot
android layout --no-idle                       # UI tree as JSON (--no-idle: the app redraws every frame)
android emulator stop medium_phone
```

The emulator uses about 2 GB of RAM while running; stop it when you're done. The
first boot takes about a minute; later boots resume from a snapshot. The SDK
with this setup takes about 4 GB of disk.

## Emulator audio is turned off

On this Mac, the headless emulator freezes and crashes as soon as it opens the host's audio
(CoreAudio hangs when the app records or when the system plays a click). So the `medium_phone`
device has audio disabled; after creating it, run:

```sh
sed -i '' 's/^hw.audioInput=.*/hw.audioInput=no/' ~/.android/avd/medium_phone.avd/config.ini
echo 'hw.audioOutput=no' >> ~/.android/avd/medium_phone.avd/config.ini
```

To see the spectrogram move on the emulator, debug builds can analyse a built-in test signal
(a sweep plus two tones) instead of the microphone:

```sh
adb shell pm grant net.galmiza.android.spectrogram.debug android.permission.RECORD_AUDIO
adb shell am start -S -n net.galmiza.android.spectrogram.debug/net.galmiza.android.spectrogram.MainActivity --ez test_signal true
```

Test real sound on a phone: enable Developer options and USB or wireless debugging, and it
shows up in `adb devices`. Debug builds are called "Spectrogram dev"
(`net.galmiza.android.spectrogram.debug`) and install next to the Play version.

The emulator also renders on the CPU (software GLES), so its frame rate says nothing about a
real phone.

## Gotchas

- **Two copies of `adb`.** Homebrew's `android-platform-tools` cask puts one on
  your `PATH`, and the SDK has its own in `~/Library/Android/sdk/platform-tools`.
  Both are 37.0.1 today. If they drift and you see "adb server version doesn't
  match this client", remove the Homebrew one
  (`brew uninstall --cask android-platform-tools`) and add
  `$ANDROID_HOME/platform-tools` to your `PATH`.
- **No Docker emulator.** On Apple Silicon the Android emulator can't run inside
  Docker (no nested virtualization). Use the native emulator.
- **`sdkmanager` / `avdmanager`** still work but are deprecated. Use
  `android sdk …` and `android emulator …` instead.
- **16 KB page-size images aren't needed.** The standard emulator image uses
  4 KB memory pages. Google Play's 16 KB requirement only concerns native
  (C/C++) libraries. The app has no C code; its only native library is a tiny
  one from Compose (`libandroidx.graphics.path.so`), already 16 KB aligned.
