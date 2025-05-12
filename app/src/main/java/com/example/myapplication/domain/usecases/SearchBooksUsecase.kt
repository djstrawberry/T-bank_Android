package com.example.myapplication.domain.usecases

import com.example.myapplication.domain.models.Book
import com.example.myapplication.domain.repositories.GoogleBooksRepository

class SearchBooksUseCase(private val repository: GoogleBooksRepository) {
    suspend operator fun invoke(author: String?, title: String?): List<Book> {
        return repository.searchBooks(author, title)
    }
}