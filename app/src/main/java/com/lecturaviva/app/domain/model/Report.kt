package com.lecturaviva.app.domain.model

data class Report(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val type: String = "",
    val message: String = "",
    val status: String = "new",
    val createdAt: Long = System.currentTimeMillis()
)
