## Synopsis

[Spectrogram-Android] is a mobile application available on the Google Play store.   
It was initially developed as a basic tool to study outdoor sound properties.  
It is and will remain free to use and ad-free.

##### Key features  

  - Realtime waveform and scrolling spectrogram of the surrounding sound
  - FFT size from 32 to 65536
  - Nine window functions
  - Linear or logarithmic frequency scale, four color palettes
  - Light/dark theme (follows the system), portrait and landscape
  - Sound is analysed on the device only: nothing is recorded or sent ([privacy policy](https://galmiza.github.io/spectrogram-android/privacy-policy.html))

##### Snapshots

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="240"> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="240"> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="240">

*Analysing a LibriVox reading of Poe's "The Raven" (public domain). Store listing texts and images are in `fastlane/metadata/android/`.*


## Build and run

The app is written in Kotlin with Jetpack Compose. To set up a Mac with
the Android SDK, the `android` command-line tool and an emulator, follow
[docs/dev-setup.md](docs/dev-setup.md). Then:

```sh
./gradlew assembleDebug
android emulator start medium_phone
android run app
```

Tests and other commands are listed in [CLAUDE.md](CLAUDE.md); releasing to Google Play is
described in [docs/release.md](docs/release.md).


## Development
Have killer features in mind you want to implement?  
You are very welcome to contribute to the project!  

## Todo
  - Add extra languages
  - Add frequency range filter
  - Add ability to play sound from the spectrogram
  - Add high resolution spectrogram export feature

## License
[zlib]

[zlib]: <https://en.wikipedia.org/wiki/Zlib_License>
[Spectrogram-Android]: <https://play.google.com/store/apps/details?id=net.galmiza.android.spectrogram>