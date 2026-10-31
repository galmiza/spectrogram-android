package net.galmiza.android.spectrogram

import android.Manifest
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Starts the app on a device with the built-in test signal and checks the main interactions.
 * Uses UiAutomator: Compose's test APIs wait for the UI to be idle, which a live spectrogram never is.
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {
    @get:Rule
    val microphone: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)

    @Test
    fun drawsTheTestSignalAndHandlesPauseAndSettings() {
        val intent = Intent(context, MainActivity::class.java).putExtra("test_signal", true)
        ActivityScenario.launch<MainActivity>(intent).use {
            val pause = By.desc(context.getString(R.string.action_pause))
            assertTrue("app did not start", device.wait(Until.hasObject(pause), TIMEOUT))

            // The first seconds of signal fill the left of the spectrogram, which starts black
            Thread.sleep(3000)
            val filled = spectrogramFill()
            assertTrue("spectrogram is not drawn (filled: $filled)", filled > 0.9)

            device.findObject(pause).click()
            assertTrue(device.wait(Until.hasObject(By.desc(context.getString(R.string.action_resume))), TIMEOUT))

            val fftSetting = By.text(context.getString(R.string.settings_fft_size))
            device.findObject(By.desc(context.getString(R.string.action_settings))).click()
            assertTrue("settings did not open", device.wait(Until.hasObject(fftSetting), TIMEOUT))
            device.pressBack()
            assertTrue("settings did not close", device.wait(Until.gone(fftSetting), TIMEOUT))
        }
    }

    /** Share of non-black pixels in the oldest columns of the spectrogram, in the lower part of a portrait screen. */
    private fun spectrogramFill(): Double {
        val screen = instrumentation.uiAutomation.takeScreenshot()
        var colored = 0
        var total = 0
        for (y in screen.height * 6 / 10 until screen.height * 9 / 10 step 4) {
            for (x in screen.width * 3 / 100 until screen.width * 12 / 100 step 4) {
                total++
                if (screen.getPixel(x, y) and 0xFFFFFF != 0) colored++
            }
        }
        return colored.toDouble() / total
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
