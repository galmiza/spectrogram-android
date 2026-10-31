plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "net.galmiza.android.spectrogram"
    compileSdk = 37

    defaultConfig {
        applicationId = "net.galmiza.android.spectrogram"
        minSdk = 26
        targetSdk = 37
        // Must stay above the last version published on Google Play (4 / 1.1.1)
        versionCode = 5
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // The upload key lives outside the repo; set its path and passwords in ~/.gradle/gradle.properties
    // (see docs/release.md). Without them, release builds are left unsigned.
    val keystore = providers.gradleProperty("spectrogramKeystore").orNull
    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = file(keystore)
                storePassword = providers.gradleProperty("spectrogramKeystorePassword").get()
                keyAlias = providers.gradleProperty("spectrogramKeyAlias").get()
                keyPassword = providers.gradleProperty("spectrogramKeyPassword").get()
            }
        }
    }

    buildTypes {
        debug {
            // Test builds install next to the Play version, as "Spectrogram dev"
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true // needed by the screenshot tests
        // Emulator created and started by Gradle for the device tests: ./gradlew phoneDebugAndroidTest
        managedDevices {
            localDevices {
                create("phone") {
                    device = "Pixel 6"
                    apiLevel = 36
                    systemImageSource = "aosp" // not "aosp-atd": its screenshots are black
                }
            }
        }
        // Renders the @PreviewTest functions in src/screenshotTest and compares them with the
        // reference images in src/screenshotTestDefaultDebug/reference
        screenshotTests.create("screenshotTest") {
            engineVersion = libs.versions.screenshot.get()
            targetVariants.add("debug")
            dependencies {
                implementation(libs.androidx.compose.ui.tooling)
                implementation(libs.screenshot.validation.api)
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.uiautomator)
}
