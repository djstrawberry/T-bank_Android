package com.example.myapplication.domain.usecases

import com.example.myapplication.domain.models.LibraryItem
import com.example.myapplication.domain.repositories.LibraryRepository

class GetLibraryItemsUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(loadMore: Boolean): Result<List<LibraryItem>> {
        return repository.getLibraryItems(loadMore)
    }
}