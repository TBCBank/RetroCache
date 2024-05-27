package ge.tbcbank.retrocache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.tbcbank.retrocache.di.CoreModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val api = CoreModule.api
    private val retroCacheManager = CoreModule.retroCacheManager

    private val _books = MutableStateFlow(BookPageDto("", "1", listOf()))
    val books: Flow<BookPageDto> = _books

    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            _books.value = api.getBookWithPage("1").body()!!
        }
    }

    fun cacheFor10Sec() {
        viewModelScope.launch(Dispatchers.IO) {
            _books.value = api.getBookWithPage(
                "1",
                cachePolicy = CachePolicy.CachedWithTime(10 * 1000L)
            ).body()!!
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _books.value = api.getBookWithPage(
                "1",
                cachePolicy = CachePolicy.Refresh
            ).body()!!
        }
    }

    fun removeFromCache() {
        retroCacheManager.clearAllByTag("mtag")
    }

    fun clearScope() {
        retroCacheManager.clearScopeCache("user_scope")
    }
}