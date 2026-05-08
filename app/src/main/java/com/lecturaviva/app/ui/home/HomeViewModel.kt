package com.lecturaviva.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lecturaviva.app.domain.model.Book
import com.lecturaviva.app.domain.model.BookStatus
import com.lecturaviva.app.domain.repository.BookRepository
import com.lecturaviva.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val userId get() = auth.currentUser?.uid ?: ""
    val userName      get() = auth.currentUser?.displayName ?: "Lector"

    val currentlyReading = bookRepo.getCurrentlyReading(userId).asLiveData()

    val countRead      = bookRepo.countByStatus(userId, BookStatus.READ).asLiveData()
    val countReading   = bookRepo.countByStatus(userId, BookStatus.READING).asLiveData()
    val countPending   = bookRepo.countByStatus(userId, BookStatus.PENDING).asLiveData()
    val countFavorites = bookRepo.countFavorites(userId).asLiveData()

    private val _updateResult = MutableStateFlow<Result<Unit>?>(null)
    val updateResult: StateFlow<Result<Unit>?> = _updateResult

    fun updateProgress(book: Book, newPage: Int) {
        if (newPage > book.totalPages) {
            _updateResult.value = Result.Error(
                "El valor ($newPage) supera el total de páginas del libro (${book.totalPages})."
            )
            return
        }
        viewModelScope.launch {
            bookRepo.updateBook(book.copy(currentPage = newPage))
            _updateResult.value = Result.Success(Unit)
        }
    }

    fun clearUpdateResult() { _updateResult.value = null }

    fun syncFromCloud() {
        viewModelScope.launch { bookRepo.syncFromFirestore(userId) }
    }
}
