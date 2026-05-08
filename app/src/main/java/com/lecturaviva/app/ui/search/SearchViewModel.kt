package com.lecturaviva.app.ui.search

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.lecturaviva.app.domain.model.Book
import com.lecturaviva.app.domain.model.BookStatus
import com.lecturaviva.app.domain.repository.BookRepository
import com.lecturaviva.app.domain.repository.SearchRepository
import com.lecturaviva.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepo: SearchRepository,
    private val bookRepo: BookRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // userId se lee en el momento de usarlo, nunca en init
    private val userId get() = auth.currentUser?.uid ?: ""

    private val _results   = MutableLiveData<List<Book>>(emptyList())
    val results: LiveData<List<Book>> = _results

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _event = MutableStateFlow<SearchEvent?>(null)
    val event: StateFlow<SearchEvent?> = _event

    private val _libraryKeys = MutableLiveData<Set<String>>(emptySet())
    val libraryKeys: LiveData<Set<String>> = _libraryKeys

    init { loadLibraryKeys() }

    private fun loadLibraryKeys() {
        viewModelScope.launch {
            bookRepo.getAllBooks(userId).collect { books ->
                _libraryKeys.value = books.mapNotNull {
                    it.openLibraryKey.ifBlank { null }
                }.toSet()
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        _isLoading.value = true
        viewModelScope.launch {
            when (val r = searchRepo.searchOpenLibrary(query)) {
                is Result.Success -> {
                    _results.value  = r.data
                    _isLoading.value = false
                    if (r.data.isEmpty()) {
                        _event.value = SearchEvent.Error("No se encontraron resultados para \"$query\".")
                    }
                }
                is Result.Error -> {
                    _isLoading.value = false
                    _event.value = SearchEvent.Error(r.message)
                }
                else -> { _isLoading.value = false }
            }
        }
    }

    fun addBook(book: Book) {
        val uid = userId
        if (uid.isEmpty()) {
            _event.value = SearchEvent.Error("No hay sesión activa. Reinicia la app.")
            return
        }
        val libraryBook = book.copy(
            userId = uid,
            status = BookStatus.PENDING
        )
        viewModelScope.launch {
            bookRepo.addBook(libraryBook)
            loadLibraryKeys()
            _event.value = SearchEvent.BookAdded(book.title)
        }
    }

    fun clearEvent() { _event.value = null }
}

sealed class SearchEvent {
    data class BookAdded(val title: String) : SearchEvent()
    data class Error(val msg: String)       : SearchEvent()
}
