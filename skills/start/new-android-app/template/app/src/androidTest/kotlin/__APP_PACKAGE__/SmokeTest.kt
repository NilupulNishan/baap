package __APP_PACKAGE__

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import __APP_PACKAGE__.data.AppDatabase
import __APP_PACKAGE__.data.ItemRepository
import __APP_PACKAGE__.settings.SettingsStore
import __APP_PACKAGE__.ui.AppRoot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Runs on a device or emulator (`./gradlew connectedDebugAndroidTest`). It is not part of
 * `./gradlew check`. It uses an in-memory database, never the app's real data.
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun launches_to_the_empty_state() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dataStore = PreferenceDataStoreFactory.create {
            File(context.cacheDir, "smoke-${System.nanoTime()}.preferences_pb")
        }
        val container = AppContainer(ItemRepository(database.itemDao()), SettingsStore(dataStore))

        compose.setContent { AppRoot(container) }

        compose.waitUntil(5_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("Nothing here yet")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Add your first item").assertExists()
        database.close()
    }
}
