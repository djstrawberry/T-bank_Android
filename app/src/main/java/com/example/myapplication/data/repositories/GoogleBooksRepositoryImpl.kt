package com.example.myapplication.data.repositories

import android.content.Context
import com.example.myapplication.data.dao.GoogleBooksApi
import com.example.myapplication.domain.models.Book
import com.example.myapplication.domain.repositories.GoogleBooksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GoogleBooksRepositoryImpl(
    context: Context? = null,
    private val api: GoogleBooksApi = createDefaultApi()
) : GoogleBooksRepository {

    override suspend fun searchBooks(author: String?, title: String?): List<Book> {
        return withContext(Dispatchers.IO) {
            try {
                val query = buildQuery(author, title)
                if (query.isEmpty()) return@withContext emptyList()

                val response = api.getBooks(query)
                if (response.isSuccessful) {
                    response.body()?.items?.mapNotNull { it?.toDomainModel() } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun buildQuery(author: String?, title: String?): String {
        val queries = mutableListOf<String>()
        author?.takeIf { it.length >= 3 }?.let { queries.add("inauthor:$it") }
        title?.takeIf { it.length >= 3 }?.let { queries.add("intitle:$it") }
        return queries.joinToString("+")
    }

    private fun com.example.myapplication.data.dao.ReceivedBook.toDomainModel(): Book {
        return Book(
            id = this.id?.hashCode() ?: 0,
            name = this.volumeInfo?.title ?: "Без названия",
            isAvailable = true,
            pages = this.volumeInfo?.pageCount ?: 0,
            author = this.volumeInfo?.authors?.joinToString(", ") ?: "Неизвестный автор"
        )
    }

    companion object {
        private fun createDefaultApi(): GoogleBooksApi {
            return Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/books/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GoogleBooksApi::class.java)
        }
    }
}