package com.lecturaviva.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.lecturaviva.app.data.local.dao.BookDao
import com.lecturaviva.app.data.local.entities.BookEntity
import com.lecturaviva.app.domain.model.Book
import com.lecturaviva.app.domain.model.BookStatus
import com.lecturaviva.app.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val firestore: FirebaseFirestore
) : BookRepository {

    override fun getAllBooks(userId: String) =
        bookDao.getAllBooks(userId).map { it.map(BookEntity::toDomain) }

    override fun getBooksByStatus(userId: String, status: BookStatus) =
        bookDao.getBooksByStatus(userId, status.name).map { it.map(BookEntity::toDomain) }

    override fun getFavoriteBooks(userId: String) =
        bookDao.getFavoriteBooks(userId).map { it.map(BookEntity::toDomain) }

    override fun searchBooks(userId: String, query: String) =
        bookDao.searchBooks(userId, query).map { it.map(BookEntity::toDomain) }

    override fun getCurrentlyReading(userId: String): Flow<Book?> =
        bookDao.getCurrentlyReading(userId).map { it?.toDomain() }

    override fun countByStatus(userId: String, status: BookStatus) =
        bookDao.countByStatus(userId, status.name)

    override fun countFavorites(userId: String) =
        bookDao.countFavorites(userId)

    override suspend fun getBookById(id: Long) =
        bookDao.getBookById(id)?.toDomain()

    override suspend fun addBook(book: Book): Long {
        // Evitar duplicados: comprobar si ya existe por openLibraryKey
        if (book.openLibraryKey.isNotEmpty()) {
            val existing = bookDao.getBookByOpenLibraryKey(book.userId, book.openLibraryKey)
            if (existing != null) return existing.id
        }
        val entity = BookEntity.fromDomain(book)
        val localId = bookDao.insertBook(entity)
        syncToFirestore(book.copy(id = localId))
        return localId
    }

    override suspend fun updateBook(book: Book) {
        bookDao.updateBook(BookEntity.fromDomain(book))
        syncToFirestore(book)
    }

    override suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(BookEntity.fromDomain(book))
        if (book.firebaseId.isNotEmpty()) {
            runCatching {
                firestore.collection("books").document(book.firebaseId).delete().await()
            }
        }
    }

    override suspend fun syncToFirestore(book: Book) {
        runCatching {
            val col = firestore.collection("books")
            val data = mapOf(
                "userId"         to book.userId,
                "title"          to book.title,
                "author"         to book.author,
                "year"           to book.year,
                "totalPages"     to book.totalPages,
                "currentPage"    to book.currentPage,
                "coverUrl"       to book.coverUrl,
                "description"    to book.description,
                "status"         to book.status.name,
                "isFavorite"     to book.isFavorite,
                "rating"         to book.rating,
                "review"         to book.review,
                "openLibraryKey" to book.openLibraryKey,
                "updatedAt"      to book.updatedAt
            )
            if (book.firebaseId.isEmpty()) {
                val ref = col.add(data).await()
                bookDao.updateBook(BookEntity.fromDomain(book.copy(firebaseId = ref.id)))
            } else {
                col.document(book.firebaseId).set(data).await()
            }
        }
    }

    override suspend fun syncFromFirestore(userId: String) {
        runCatching {
            val snapshot = firestore.collection("books")
                .whereEqualTo("userId", userId)
                .get().await()

            snapshot.documents.forEach { doc ->
                // Comprobar si ya existe localmente por firebaseId — NO insertar duplicado
                val existing = bookDao.getBookByFirebaseId(doc.id)
                if (existing != null) {
                    // Ya existe — actualizar con datos de la nube si son más recientes
                    val cloudUpdatedAt = doc.getLong("updatedAt") ?: 0L
                    if (cloudUpdatedAt > existing.updatedAt) {
                        bookDao.updateBook(existing.copy(
                            status      = doc.getString("status") ?: existing.status,
                            isFavorite  = doc.getBoolean("isFavorite") ?: existing.isFavorite,
                            rating      = (doc.getDouble("rating") ?: existing.rating.toDouble()).toFloat(),
                            review      = doc.getString("review") ?: existing.review,
                            currentPage = (doc.getLong("currentPage") ?: existing.currentPage.toLong()).toInt(),
                            updatedAt   = cloudUpdatedAt
                        ))
                    }
                } else {
                    // No existe — insertar solo si no hay duplicado por openLibraryKey
                    val openLibraryKey = doc.getString("openLibraryKey") ?: ""
                    val existingByKey = if (openLibraryKey.isNotEmpty())
                        bookDao.getBookByOpenLibraryKey(userId, openLibraryKey) else null

                    if (existingByKey == null) {
                        bookDao.insertBook(BookEntity(
                            firebaseId     = doc.id,
                            userId         = userId,
                            title          = doc.getString("title") ?: "",
                            author         = doc.getString("author") ?: "",
                            year           = (doc.getLong("year") ?: 0).toInt(),
                            totalPages     = (doc.getLong("totalPages") ?: 0).toInt(),
                            currentPage    = (doc.getLong("currentPage") ?: 0).toInt(),
                            coverUrl       = doc.getString("coverUrl") ?: "",
                            description    = doc.getString("description") ?: "",
                            status         = doc.getString("status") ?: BookStatus.PENDING.name,
                            isFavorite     = doc.getBoolean("isFavorite") ?: false,
                            rating         = (doc.getDouble("rating") ?: 0.0).toFloat(),
                            review         = doc.getString("review") ?: "",
                            openLibraryKey = openLibraryKey,
                            updatedAt      = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                        ))
                    }
                }
            }
        }
    }
}