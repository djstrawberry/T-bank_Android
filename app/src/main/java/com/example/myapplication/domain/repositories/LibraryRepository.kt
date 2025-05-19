package com.example.myapplication.domain.repositories

import com.example.myapplication.domain.models.Book
import com.example.myapplication.domain.models.Disk
import com.example.myapplication.domain.models.LibraryItem
import com.example.myapplication.domain.models.Newspaper

interface LibraryRepository {
    suspend fun getLibraryItems(loadMore: Boolean = false): Result<List<LibraryItem>>
    suspend fun addItem(item: LibraryItem): Result<Boolean>
    suspend fun getBook(id: Int): Book?
    suspend fun getNewspaper(id: Int): Newspaper?
    suspend fun getDisk(id: Int): Disk?
    fun getNextAvailableId(): Int
    suspend fun setSortOrder(order: SortOrder)

    enum class SortOrder { BY_NAME, BY_DATE }
}