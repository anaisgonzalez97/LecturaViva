package com.lecturaviva.app.ui.detail

import androidx.lifecycle.*
import com.lecturaviva.app.domain.model.Book
import com.lecturaviva.app.domain.model.BookStatus
import com.lecturaviva.app.domain.model.Note
import com.lecturaviva.app.domain.repository.BookRepository
import com.lecturaviva.app.domain.repository.NoteRepository
import com.lecturaviva.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val noteRepo: NoteRepository
) : ViewModel() {

    private val _book = MutableLiveData<Book?>()
    val book: LiveData<Book?> = _book

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _uiEvent = MutableStateFlow<DetailEvent?>(null)
    val uiEvent: StateFlow<DetailEvent?> = _uiEvent

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            val b = bookRepo.getBookById(bookId)
            _book.value = b
            if (b != null) {
                noteRepo.getNotesByBook(bookId).collect { _notes.value = it }
            }
        }
    }

    fun updateProgress(newPage: Int) {
        val b = _book.value ?: return
        if (newPage > b.totalPages) {
            _uiEvent.value = DetailEvent.Error(
                "El valor ($newPage) supera el total de páginas del libro (${b.totalPages})."
            )
            return
        }
        viewModelScope.launch {
            val updated = b.copy(currentPage = newPage)
            bookRepo.updateBook(updated)
            _book.value = updated
            _uiEvent.value = DetailEvent.ProgressUpdated
        }
    }

    fun updateStatus(status: BookStatus) {
        val b = _book.value ?: return
        viewModelScope.launch {
            val updated = if (status == BookStatus.READ) {
                b.copy(status = status, currentPage = b.totalPages)
            } else {
                b.copy(status = status)
            }
            bookRepo.updateBook(updated)
            _book.value = updated
            _uiEvent.value = if (status == BookStatus.READ) DetailEvent.BookFinished
            else DetailEvent.StatusUpdated(status)
        }
    }

    fun toggleFavorite() {
        val b = _book.value ?: return
        viewModelScope.launch {
            val updated = b.copy(isFavorite = !b.isFavorite)
            bookRepo.updateBook(updated)
            _book.value = updated
        }
    }

    fun saveRating(stars: Float) {
        val b = _book.value ?: return
        viewModelScope.launch {
            val updated = b.copy(rating = stars)
            bookRepo.updateBook(updated)
            _book.value = updated
            _uiEvent.value = DetailEvent.RatingSaved(stars)
        }
    }

    fun saveReview(text: String) {
        val b = _book.value ?: return
        viewModelScope.launch {
            val updated = b.copy(review = text)
            bookRepo.updateBook(updated)
            _book.value = updated
            _uiEvent.value = DetailEvent.ReviewSaved
        }
    }

    fun addNote(text: String, chapter: Int = 0, page: Int = 0) {
        val b = _book.value ?: return
        if (text.isBlank()) {
            _uiEvent.value = DetailEvent.Error("Escribe algo antes de guardar la nota.")
            return
        }
        viewModelScope.launch {
            noteRepo.addNote(Note(bookId = b.id, text = text, chapter = chapter, page = page))
            _uiEvent.value = DetailEvent.NoteAdded
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { noteRepo.deleteNote(note) }
    }

    fun deleteBook() {
        val b = _book.value ?: return
        viewModelScope.launch {
            bookRepo.deleteBook(b)
            _uiEvent.value = DetailEvent.BookDeleted
        }
    }

    fun clearEvent() { _uiEvent.value = null }
}

sealed class DetailEvent {
    object ProgressUpdated                     : DetailEvent()
    object ReviewSaved                         : DetailEvent()
    object NoteAdded                           : DetailEvent()
    object BookFinished                        : DetailEvent()
    object BookDeleted                         : DetailEvent()
    data class StatusUpdated(val s: BookStatus): DetailEvent()
    data class RatingSaved(val stars: Float)   : DetailEvent()
    data class Error(val msg: String)          : DetailEvent()
}