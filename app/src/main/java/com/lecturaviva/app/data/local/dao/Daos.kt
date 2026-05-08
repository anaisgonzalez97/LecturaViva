package com.lecturaviva.app.data.local.dao

import androidx.room.*
import com.lecturaviva.app.data.local.entities.BookEntity
import com.lecturaviva.app.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getAllBooks(userId: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE userId = :userId AND status = :status ORDER BY updatedAt DESC")
    fun getBooksByStatus(userId: String, status: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE userId = :userId AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteBooks(userId: String): Flow<List<BookEntity>>

    @Query("""SELECT * FROM books WHERE userId = :userId
              AND (LOWER(title) LIKE '%' || LOWER(:query) || '%'
                OR LOWER(author) LIKE '%' || LOWER(:query) || '%')
              ORDER BY updatedAt DESC""")
    fun searchBooks(userId: String, query: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): BookEntity?

    // Buscar por firebaseId para evitar duplicados en sync
    @Query("SELECT * FROM books WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getBookByFirebaseId(firebaseId: String): BookEntity?

    // Buscar por openLibraryKey para evitar duplicados al añadir
    @Query("SELECT * FROM books WHERE userId = :userId AND openLibraryKey = :key AND openLibraryKey != '' LIMIT 1")
    suspend fun getBookByOpenLibraryKey(userId: String, key: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE userId = :userId")
    suspend fun deleteAllBooksForUser(userId: String)

    // Estadísticas
    @Query("SELECT COUNT(*) FROM books WHERE userId = :userId AND status = :status")
    fun countByStatus(userId: String, status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM books WHERE userId = :userId AND isFavorite = 1")
    fun countFavorites(userId: String): Flow<Int>

    @Query("SELECT * FROM books WHERE userId = :userId AND status = 'READING' ORDER BY updatedAt DESC LIMIT 1")
    fun getCurrentlyReading(userId: String): Flow<BookEntity?>
}

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getNotesByBook(bookId: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE bookId = :bookId")
    suspend fun deleteNotesForBook(bookId: Long)
}