package __APP_PACKAGE__

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import __APP_PACKAGE__.data.AppDatabase
import __APP_PACKAGE__.data.ItemRepository
import __APP_PACKAGE__.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * A container for tests. Its database lives in memory (or in a temp file you pass in)
 * and its settings in a temp folder, so a test never touches real user data.
 * Seed data lives only in src/test, which keeps it out of the app.
 */
class TestEnv(val container: AppContainer, val database: AppDatabase)

fun testContainer(context: Context, databaseFile: File? = null): TestEnv {
    val database = if (databaseFile == null) {
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    } else {
        Room.databaseBuilder(context, AppDatabase::class.java, databaseFile.absolutePath).build()
    }
    val settingsFile = File(createTempDirectory("settings").toFile(), "settings.preferences_pb")
    val dataStore = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { settingsFile },
    )
    val container = AppContainer(ItemRepository(database.itemDao()), SettingsStore(dataStore))
    return TestEnv(container, database)
}
