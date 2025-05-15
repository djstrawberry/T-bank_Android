package com.example.myapplication.domain.usecases

import com.example.myapplication.domain.models.LibraryItem
import com.example.myapplication.domain.repositories.LibraryRepository

class AddItemUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(item: LibraryItem): Result<Boolean> {
        return repository.addItem(item)
    }
}