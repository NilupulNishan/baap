package __APP_PACKAGE__.data

import kotlinx.coroutines.flow.Flow

/** The only way screens reach the database. Room runs these off the main thread. */
class ItemRepository(private val dao: ItemDao) {
    fun observeAll(): Flow<List<Item>> = dao.observeAll()

    fun observeStarred(): Flow<List<Item>> = dao.observeStarred()

    fun observe(id: Long): Flow<Item?> = dao.observe(id)

    suspend fun add(title: String = ""): Long = dao.insert(Item(title = title))

    suspend fun rename(id: Long, title: String) = dao.rename(id, title)

    suspend fun setStarred(id: Long, starred: Boolean) = dao.setStarred(id, starred)

    /** Deletes the item and returns it, so the caller can offer Undo. */
    suspend fun delete(id: Long): Item? {
        val item = dao.get(id)
        dao.delete(id)
        return item
    }

    suspend fun restore(item: Item) {
        dao.insert(item)
    }
}
