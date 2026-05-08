package com.lecturaviva.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lecturaviva.app.domain.model.Book
import com.lecturaviva.app.domain.model.BookStatus

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firebaseId: String = "",
    val userId: String = "",
    val title: String,
    val author: String,
    val year: Int = 0,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val coverUrl: String = "",
    val description: String = "",
    val status: String = BookStatus.PENDING.name,
    val isFavorite: Boolean = false,
    val rating: Float = 0f,
    val review: String = "",
    val openLibraryKey: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Book(
        id          = id,
        firebaseId  = firebaseId,
        userId      = userId,
        title       = title,
        author      = author,
        year        = year,
        totalPages  = totalPages,
        currentPage = currentPage,
        coverUrl    = coverUrl,
        description = description,
        status      = BookStatus.fromString(status),
        isFavorite  = isFavorite,
        rating      = rating,
        review      = review,
        openLibraryKey = openLibraryKey,
        createdAt   = createdAt,
        updatedAt   = updatedAt
    )

    companion object {
        fun fromDomain(b: Book) = BookEntity(
            id          = b.id,
            firebaseId  = b.firebaseId,
            userId      = b.userId,
            title       = b.title,
            author      = b.author,
            year        = b.year,
            totalPages  = b.totalPages,
            currentPage = b.currentPage,
            coverUrl    = b.coverUrl,
            description = b.description,
            status      = b.status.name,
            isFavorite  = b.isFavorite,
            rating      = b.rating,
            review      = b.review,
            openLibraryKey = b.openLibraryKey,
            createdAt   = b.createdAt,
            updatedAt   = System.currentTimeMillis()
        )
    }
}
