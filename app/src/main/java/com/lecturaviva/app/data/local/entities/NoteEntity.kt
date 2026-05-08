package com.lecturaviva.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lecturaviva.app.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val text: String,
    val chapter: Int = 0,    // 0 = no especificado
    val page: Int = 0,       // 0 = no especificada
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Note(id = id, bookId = bookId, text = text, chapter = chapter, page = page, createdAt = createdAt)

    companion object {
        fun fromDomain(n: Note) = NoteEntity(id = n.id, bookId = n.bookId, text = n.text, chapter = n.chapter, page = n.page, createdAt = n.createdAt)
    }
}