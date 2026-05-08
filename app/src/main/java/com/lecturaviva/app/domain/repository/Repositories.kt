package com.lecturaviva.app.domain.repository

import com.lecturaviva.app.domain.model.Book
import com.lecturaviva.app.domain.model.BookStatus
import com.lecturaviva.app.domain.model.Note
import com.lecturaviva.app.domain.model.User
import com.lecturaviva.app.util.Result
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(userId: String): Flow<List<Book>>
    fun getBooksByStatus(userId: String, status: BookStatus): Flow<List<Book>>
    fun getFavoriteBooks(userId: String): Flow<List<Book>>
    fun searchBooks(userId: String, query: String): Flow<List<Book>>
    fun getCurrentlyReading(userId: String): Flow<Book?>
    fun countByStatus(userId: String, status: BookStatus): Flow<Int>
    fun countFavorites(userId: String): Flow<Int>
    suspend fun getBookById(id: Long): Book?
    suspend fun addBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(book: Book)
    suspend fun syncToFirestore(book: Book)
    suspend fun syncFromFirestore(userId: String)
}

interface NoteRepository {
    fun getNotesByBook(bookId: Long): Flow<List<Note>>
    suspend fun addNote(note: Note): Long
    suspend fun deleteNote(note: Note)
}

interface AuthRepository {
    val currentUser: User?
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updateProfile(name: String, photoUrl: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    fun logout()
}

interface SearchRepository {
    suspend fun searchOpenLibrary(query: String): Result<List<Book>>
}