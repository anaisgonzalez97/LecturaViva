package com.lecturaviva.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.lecturaviva.app.domain.model.Book

data class OpenLibraryResponse(
    @SerializedName("numFound") val numFound: Int = 0,
    @SerializedName("docs")     val docs: List<BookDto> = emptyList()
)

data class BookDto(
    @SerializedName("key")                      val key: String = "",
    @SerializedName("title")                    val title: String = "",
    @SerializedName("author_name")              val authorName: List<String>? = null,
    @SerializedName("cover_i")                  val coverId: Long? = null,
    @SerializedName("first_publish_year")       val firstPublishYear: Int? = null,
    @SerializedName("number_of_pages_median")   val numberOfPages: Int? = null,
    @SerializedName("subject")                  val subjects: List<String>? = null
) {
    val coverUrl: String
        get() = if (coverId != null) "https://covers.openlibrary.org/b/id/$coverId-M.jpg" else ""

    fun toDomain() = Book(
        title          = title,
        author         = authorName?.firstOrNull() ?: "Autor desconocido",
        year           = firstPublishYear ?: 0,
        totalPages     = numberOfPages ?: 0,
        coverUrl       = coverUrl,
        openLibraryKey = key
    )
}
