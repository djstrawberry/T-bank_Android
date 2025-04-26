package com.example.myapplication

import kotlinx.coroutines.delay
import kotlin.random.Random

class LibraryRepository {
    private val library = Library.getInstance()

    suspend fun getLibraryItems(): Result<List<LibraryItem>> {
        try {
            delay(Random.nextLong(100, 2000))

            if (Random.nextInt(1, 10) == 2) {
                throw Exception("Данные не были загружены.")
            }

            return Result.success(library.libraryItems)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun addItem(item: LibraryItem): Result<Boolean> {
        try {
            delay(Random.nextLong(100, 2000))

            if (Random.nextInt(1, 10) == 2) {
                throw Exception("Ошибка при добавлении элемента.")
            }

            library.libraryItems.add(item)
            return Result.success(true)
        } catch(e: Exception) {
            return Result.failure(e)
        }
    }
}