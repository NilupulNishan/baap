package __APP_PACKAGE__

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import __APP_PACKAGE__.ui.AppRoot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Takes one PNG per page, in light and dark, into shots/ (`./gradlew snap`).
 * Look at every PNG before you call a UI change done.
 * Add a test that opens a dialog, sheet, or menu when a story adds one.
 */
private fun shotFile(name: String): File {
    val dir = File(System.getProperty("shots.dir") ?: "build/shots")
    dir.mkdirs()
    return File(dir, "$name.png")
}

abstract class SnapBase(private val page: Page, private val dark: Boolean, private val suffix: String) {
    @get:Rule
    val compose = createComposeRule()

    private val env = testContainer(ApplicationProvider.getApplicationContext())

    @After
    fun tearDown() {
        env.database.close()
    }

    @Test
    fun snapshot() {
        runBlocking { env.database.seed() }
        compose.setContent { AppRoot(env.container, startPage = page, darkTheme = dark) }
        when (page) {
            Page.All -> compose.waitForText(seedItems.first().title)
            Page.Starred -> compose.waitForText(seedItems.first { it.starred }.title)
            Page.Settings -> compose.waitForIdle()
        }
        val mode = if (dark) "dark" else "light"
        compose.onRoot().captureRoboImage(shotFile("${page.name.lowercase()}$suffix-$mode"))
    }

    companion object {
        fun pages(): List<Array<Any>> =
            Page.entries.flatMap { page -> listOf(false, true).map { dark -> arrayOf<Any>(page, dark) } }
    }
}

/** A phone: bottom navigation bar. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xhdpi")
class SnapTest(page: Page, dark: Boolean) : SnapBase(page, dark, "") {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-dark={1}")
        fun params(): List<Array<Any>> = pages()
    }
}

/** A tablet: the bar becomes a navigation rail. */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(qualifiers = "w900dp-h700dp-xhdpi")
class SnapTabletTest(page: Page, dark: Boolean) : SnapBase(page, dark, "-tablet") {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-dark={1}")
        fun params(): List<Array<Any>> = pages()
    }
}
