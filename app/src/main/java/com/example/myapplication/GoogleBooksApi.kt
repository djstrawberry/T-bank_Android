package com.example.myapplication

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {
    @GET("volumes")
    suspend fun getBooks(
        @Query("q")
        query: String,
        @Query("maxResults")
        maxResults: Int = 20,
        @Query("fields")
        fields: String = "items(id,volumeInfo(title,authors,pageCount,industryIdentifiers))"
    ): Response<GoogleBooks>
}