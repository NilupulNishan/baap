package __APP_PACKAGE__.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The one record type the template stores. Rename it to the brief's main type
 * (a Task, a Note) and add fields as stories need them.
 *
 * A new field needs a default value and a database version bump (see AppDatabase).
 */
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val starred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
