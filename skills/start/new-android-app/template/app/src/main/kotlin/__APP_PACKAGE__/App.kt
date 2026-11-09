package __APP_PACKAGE__

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import __APP_PACKAGE__.data.AppDatabase
import __APP_PACKAGE__.data.ItemRepository
import __APP_PACKAGE__.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Everything the screens need, built by hand. Tests build their own with an in-memory database. */
class AppContainer(
    val repository: ItemRepository,
    val settings: SettingsStore,
) {
    companion object {
        fun create(context: Context): AppContainer {
            val app = context.applicationContext
            val database = Room.databaseBuilder(app, AppDatabase::class.java, "app.db").build()
            val dataStore = PreferenceDataStoreFactory.create(
                produceFile = { app.preferencesDataStoreFile("settings") },
            )
            return AppContainer(ItemRepository(database.itemDao()), SettingsStore(dataStore))
        }
    }
}

class App : Application() {
    /** Work that outlives any screen runs here. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer.create(this)
        // baap:scanner-start
    }
}
