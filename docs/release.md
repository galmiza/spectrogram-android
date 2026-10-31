# Releasing to Google Play

Package: `net.galmiza.android.spectrogram`. Store listing texts and images are in
`fastlane/metadata/android/` (English and French). Play Console menu names may vary slightly.

## One-time setup

1. **Signing key.** Add the upload key to `~/.gradle/gradle.properties` (outside the repo, never
   committed):

   ```properties
   spectrogramKeystore=/absolute/path/to/android_market.key
   spectrogramKeystorePassword=...
   spectrogramKeyAlias=gi_key01
   spectrogramKeyPassword=...
   ```

   Keep two backups of the keystore and its passwords (password manager and an offline copy).
   Its SHA-256 fingerprint starts with `A0:EA:28` and ends with `A7:D4:D8`.

2. **Play App Signing.** App bundles require it. In Play Console, open *Test and release → App
   integrity → Play app signing*. If the app is not enrolled yet, choose to use your existing app
   signing key and follow the steps (Google's PEPK tool encrypts the key for upload).

3. **Privacy policy.** Publish `docs/privacy-policy.html` at a stable HTTPS address (for example
   with GitHub Pages, or on galmiza.net) and enter it under *Policy and programs → App content →
   Privacy policy*. Not a PDF; the page must stay reachable.

## Each release

1. In `app/build.gradle.kts`, increase `versionCode` by one and update `versionName`. Write the
   release notes in `fastlane/metadata/android/{en-US,fr-FR}/changelogs/<versionCode>.txt`
   (500 characters max).
2. Check: `./gradlew testDebugUnitTest testDebugScreenshotTestDefaultTestSuite lintRelease phoneDebugAndroidTest`.
3. Build the bundle: `./gradlew bundleRelease`, then check it is signed with your key:
   `keytool -printcert -jarfile app/build/outputs/bundle/release/app-release.aab`.
4. Upload `app-release.aab` to *Internal testing* first, install it from Play on your phone, then
   promote the release to *Production*.
5. If the listing changed, copy the texts and upload the screenshots from `fastlane/metadata/android`.
   Screenshots must be at most twice as long as they are wide (1080×2160 works).

## Store screenshots

The screenshots in `fastlane/metadata/android/en-US/images/phoneScreenshots` come from the
emulator analysing a real voice: a LibriVox reading of Poe's "The Raven" (public domain), converted
to a 16-bit mono 44.1 kHz WAV. To make new ones:

```sh
adb push voice.wav /data/local/tmp/voice.wav
adb shell run-as net.galmiza.android.spectrogram.debug sh -c 'mkdir -p files && cp /data/local/tmp/voice.wav files/'
adb shell wm size 1080x2160   # Play accepts at most a 2:1 aspect ratio
adb shell am start -S -n net.galmiza.android.spectrogram.debug/net.galmiza.android.spectrogram.MainActivity \
    --es test_audio /data/user/0/net.galmiza.android.spectrogram.debug/files/voice.wav
android screen capture -o 1.png
adb shell wm size reset
```

Android's demo mode (`adb shell am broadcast -a com.android.systemui.demo -e command enter`, then
`clock -e hhmm 1200`, `notifications -e visible false`…) gives a clean status bar.

## Play Console answers (App content)

| Section | Answer |
|---|---|
| Privacy policy | The URL from setup step 3 |
| Ads | No, the app has no ads |
| App access | All functionality is available without special access |
| Content rating | Category "Utility, Productivity, Communication, or Other"; every question "No" |
| Target audience | 13–15, 16–17 and 18+ (not designed for children, so the Families policy does not apply) |
| News app | No |
| Data safety | No data collected and no data shared: sound is analysed on the device only, settings stay on the device |
| Advertising ID | No, the app does not use it |
| Government, financial, health | No / none |

The microphone permission (`RECORD_AUDIO`) needs no special declaration form.

## Getting the app back on Play

Google removed version 1.1.1 for policy issues. Read each issue under *Policy and programs →
Policy status* and fix it: likely candidates are the privacy policy link (galmiza.net was down),
the Data safety form and the App content declarations above. Then upload version 2.0 (it targets
API 37, above Play's minimum of 36) and send the changes for review from *Publishing overview*.
