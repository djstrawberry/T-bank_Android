package com.example.myapplication

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GoogleBooksRepository(context: Context) {
    private val api: GoogleBooksApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleBooksApi::class.java)
    }
    private val context = context.applicationContext

    suspend fun searchBooks(author: String?, title: String?): List<Book> {
        return try {
            val query = buildQuery(author, title)
            if (query.isEmpty()) return emptyList()

            val response = withContext(Dispatchers.IO) { api.getBooks(query) }

            if (response.isSuccessful) {
                response.body()?.books?.map { it.toBook() } ?: emptyList()
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка API: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
                emptyList()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            emptyList()
        }
    }

    private fun buildQuery(author: String?, title: String?): String {
        val queries = mutableListOf<String>()
        author?.takeIf { it.length >= 3 }?.let { queries.add("inauthor:$it") }
        title?.takeIf { it.length >= 3 }?.let { queries.add("intitle:$it") }

        return queries.joinToString("+")
    }
}