package com.lecturaviva.app.data.remote.api

import com.lecturaviva.app.data.remote.dto.OpenLibraryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q")      query: String,
        @Query("limit")  limit: Int = 30,
        @Query("fields") fields: String = "key,title,author_name,cover_i,first_publish_year,number_of_pages_median,subject"
    ): OpenLibraryResponse
}
