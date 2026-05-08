package com.lecturaviva.app.domain.model

data class Book(
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
    val status: BookStatus = BookStatus.PENDING,
    val isFavorite: Boolean = false,
    val rating: Float = 0f,
    val review: String = "",
    val openLibraryKey: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Int
        get() = if (totalPages > 0) ((currentPage.toFloat() / totalPages) * 100).toInt() else 0
}

enum class BookStatus(val displayName: String) {
    READING("Leyendo"),
    PENDING("Pendiente"),
    READ("Leído"),
    ABANDONED("Abandonado");

    companion object {
        fun fromString(value: String) = entries.find { it.name == value } ?: PENDING
    }
}
