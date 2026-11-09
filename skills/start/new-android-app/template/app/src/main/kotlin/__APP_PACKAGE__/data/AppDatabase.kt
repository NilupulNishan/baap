package __APP_PACKAGE__.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Version 1. To change the schema, raise the version and add a migration
 * (an AutoMigration for simple changes). Room writes each version to
 * app/schemas/, and those files go into git.
 */
@Database(entities = [Item::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
