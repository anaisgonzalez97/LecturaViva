package com.lecturaviva.app.domain.model

data class Note(
    val id: Long = 0,
    val bookId: Long,
    val text: String,
    val chapter: Int = 0,   // 0 = no especificado
    val page: Int = 0,      // 0 = no especificada
    val createdAt: Long = System.currentTimeMillis()
)

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = ""
)