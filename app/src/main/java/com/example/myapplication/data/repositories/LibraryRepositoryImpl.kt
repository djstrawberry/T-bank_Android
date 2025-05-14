package com.example.myapplication.data.repositories

import androidx.room.withTransaction
import com.example.myapplication.data.dao.BookEntity
import com.example.myapplication.data.dao.DatabaseLibrary
import com.example.myapplication.data.dao.DiskEntity
import com.example.myapplication.data.dao.LibraryItemEntity
import com.example.myapplication.data.dao.NewspaperEntity
import com.example.myapplication.domain.models.Book
import com.example.myapplication.domain.models.Disk
import com.example.myapplication.domain.models.LibraryItem
import com.example.myapplication.domain.models.Newspaper
import com.example.myapplication.domain.repositories.LibraryRepository
import com.example.myapplication.data.dao.BookDao
import com.example.myapplication.data.dao.DiskDao
import com.example.myapplication.data.dao.LibraryItemDao
import com.example.myapplication.data.dao.NewspaperDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.random.Random

class LibraryRepositoryImpl(
    private val db: DatabaseLibrary
) : LibraryRepository {

    private val libraryItemDao: LibraryItemDao = db.libraryItemDao()
    private val bookDao: BookDao = db.bookDao()
    private val newspaperDao: NewspaperDao = db.newspaperDao()
    private val diskDao: DiskDao = db.diskDao()

    private var currentSortOrder: LibraryRepository.SortOrder = LibraryRepository.SortOrder.BY_NAME
    private var currentOffset = 0
    private val currentLimit = 30
    private val loadedItems = mutableListOf<LibraryItem>()

    override suspend fun getLibraryItems(loadMore: Boolean): Result<List<LibraryItem>> = withContext(Dispatchers.IO) {
        try {
            if (!loadMore) {
                currentOffset = 0
                loadedItems.clear()
            }

            val newItems = getItemsFromDb(currentLimit, currentOffset)

            if (loadMore && newItems.isNotEmpty()) {
                currentOffset += currentLimit
                loadedItems.addAll(newItems)
            } else if (!loadMore) {
                loadedItems.addAll(newItems)
            }

            Result.success(loadedItems.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getItemsFromDb(limit: Int, offset: Int): List<LibraryItem> {
        val itemEntities = when (currentSortOrder) {
            LibraryRepository.SortOrder.BY_NAME -> libraryItemDao.getItemsSortedByName(limit, offset)
            LibraryRepository.SortOrder.BY_DATE -> libraryItemDao.getItemsSortedByDate(limit, offset)
        }

        return itemEntities.mapNotNull { entity ->
            when (entity.item) {
                "Book" -> bookDao.getBook(entity.id)?.toDomainModel()
                "Newspaper" -> newspaperDao.getNewspaper(entity.id)?.toDomainModel()
                "Disk" -> diskDao.getDisk(entity.id)?.toDomainModel()
                else -> null
            }
        }
    }

    override suspend fun addItem(item: LibraryItem): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                when (item) {
                    is Book -> {
                        val entity = item.toEntity()
                        libraryItemDao.insertItem(entity.toLibraryItemEntity())
                        bookDao.insertBook(entity)
                    }
                    is Newspaper -> {
                        val entity = item.toEntity()
                        libraryItemDao.insertItem(entity.toLibraryItemEntity())
                        newspaperDao.insertNewspaper(entity)
                    }
                    is Disk -> {
                        val entity = item.toEntity()
                        libraryItemDao.insertItem(entity.toLibraryItemEntity())
                        diskDao.insertDisk(entity)
                    }
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBook(id: Int): Book? = withContext(Dispatchers.IO) {
        bookDao.getBook(id)?.toDomainModel()
    }

    override suspend fun getNewspaper(id: Int): Newspaper? = withContext(Dispatchers.IO) {
        newspaperDao.getNewspaper(id)?.toDomainModel()
    }

    override suspend fun getDisk(id: Int): Disk? = withContext(Dispatchers.IO) {
        diskDao.getDisk(id)?.toDomainModel()
    }

    override fun getNextAvailableId(): Int {
        return Random.nextInt(100000)
    }

    override suspend fun setSortOrder(order: LibraryRepository.SortOrder) {
        currentSortOrder = order
        currentOffset = 0
        loadedItems.clear()
    }

    private fun Book.toEntity(): BookEntity = BookEntity(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        pages = this.pages,
        author = this.author,
        createdAt = Date()
    )

    private fun BookEntity.toDomainModel(): Book = Book(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        pages = this.pages,
        author = this.author
    )

    private fun Newspaper.toEntity(): NewspaperEntity = NewspaperEntity(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        month = this.month,
        issueNumber = this.issueNumber,
        createdAt = Date()
    )

    private fun NewspaperEntity.toDomainModel(): Newspaper = Newspaper(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        month = this.month,
        issueNumber = this.issueNumber
    )

    private fun Disk.toEntity(): DiskEntity = DiskEntity(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        type = this.type,
        createdAt = Date()
    )

    private fun DiskEntity.toDomainModel(): Disk = Disk(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        type = this.type
    )

    private fun BookEntity.toLibraryItemEntity(): LibraryItemEntity = LibraryItemEntity(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        item = "Book",
        createdAt = this.createdAt
    )

    private fun NewspaperEntity.toLibraryItemEntity(): LibraryItemEntity = LibraryItemEntity(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        item = "Newspaper",
        createdAt = this.createdAt
    )

    private fun DiskEntity.toLibraryItemEntity(): LibraryItemEntity = LibraryItemEntity(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        item = "Disk",
        createdAt = this.createdAt
    )
}