package com.example.myapplication.domain.repositories

import com.example.myapplication.domain.models.Book

interface GoogleBooksRepository {
    suspend fun searchBooks(author: String?, title: String?): List<Book>
}