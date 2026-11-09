package __APP_PACKAGE__.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import __APP_PACKAGE__.data.Item
import __APP_PACKAGE__.data.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Lists are null while they load, so a page never flashes its empty state
 * before the first rows arrive.
 */
class ItemsViewModel(private val repository: ItemRepository) : ViewModel() {
    val all: StateFlow<List<Item>?> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val starred: StateFlow<List<Item>?> = repository.observeStarred()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun item(id: Long): Flow<Item?> = repository.observe(id)

    suspend fun add(): Long = repository.add()

    fun rename(id: Long, title: String) {
        viewModelScope.launch { repository.rename(id, title) }
    }

    fun setStarred(id: Long, starred: Boolean) {
        viewModelScope.launch { repository.setStarred(id, starred) }
    }

    suspend fun delete(id: Long): Item? = repository.delete(id)

    fun restore(item: Item) {
        viewModelScope.launch { repository.restore(item) }
    }

    companion object {
        fun factory(repository: ItemRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ItemsViewModel(repository) }
        }
    }
}
