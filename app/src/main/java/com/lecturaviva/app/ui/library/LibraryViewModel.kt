package com.lecturaviva.app.ui.library

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.lecturaviva.app.domain.model.Book
import com.lecturaviva.app.domain.model.BookStatus
import com.lecturaviva.app.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val userId get() = auth.currentUser?.uid ?: ""

    // Filtro activo
    private val _filter = MutableStateFlow<LibraryFilter>(LibraryFilter.All)
    val filter: StateFlow<LibraryFilter> = _filter

    // Query de búsqueda interna
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    @OptIn(ExperimentalCoroutinesApi::class)
    val books: LiveData<List<Book>> = combine(_filter, _query) { f, q -> Pair(f, q) }
        .flatMapLatest { (filter, query) ->
            val userId = this.userId
            when {
                query.isNotBlank() -> bookRepo.searchBooks(userId, query)
                filter == LibraryFilter.All       -> bookRepo.getAllBooks(userId)
                filter == LibraryFilter.Favorites -> bookRepo.getFavoriteBooks(userId)
                else -> bookRepo.getBooksByStatus(userId, filter.toStatus()!!)
            }
        }
        .asLiveData()

    fun setFilter(f: LibraryFilter) { _filter.value = f }
    fun setQuery(q: String)         { _query.value  = q }

    fun toggleFavorite(book: Book) {
        viewModelScope.launch { bookRepo.updateBook(book.copy(isFavorite = !book.isFavorite)) }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch { bookRepo.deleteBook(book) }
    }
}

enum class LibraryFilter(val label: String) {
    All("Todos"),
    Reading("Leyendo"),
    Pending("Pendientes"),
    Read("Leídos"),
    Favorites("Favoritos"),
    Abandoned("Abandonados");

    fun toStatus(): BookStatus? = when (this) {
        Reading   -> BookStatus.READING
        Pending   -> BookStatus.PENDING
        Read      -> BookStatus.READ
        Abandoned -> BookStatus.ABANDONED
        else      -> null
    }
}
