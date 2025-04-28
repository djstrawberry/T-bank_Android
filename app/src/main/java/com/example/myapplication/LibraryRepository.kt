package com.example.myapplication

import androidx.room.withTransaction
import kotlin.random.Random

class LibraryRepository(private val db: DatabaseLibrary) {
    enum class SortOrder {
        BY_NAME, BY_DATE
    }

    private val libraryItemDao = db.libraryItemDao()
    private val bookDao = db.bookDao()
    private val newspaperDao = db.newspaperDao()
    private val diskDao = db.diskDao()

    private var currentSortOrder: SortOrder = SortOrder.BY_NAME
    private var currentOffset = 0
    private var currentLimit = 30
    private var loadedItems = mutableListOf<LibraryItem>()


    suspend fun getLibraryItems(
        loadMore: Boolean = false
    ): Result<List<LibraryItem>> {
        return try {
            if (!loadMore) {
                currentOffset = 0
                loadedItems.clear()
            }

            val newItems = getItemsFromDb(currentLimit, currentOffset)

            if (loadMore) {
                if (newItems.isNotEmpty()) {
                    currentOffset += currentLimit
                    loadedItems.addAll(newItems)
                }
            } else {
                loadedItems = newItems.toMutableList()
            }

            Result.success(loadedItems.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNextAvailableId(): Int {
        val random = Random(System.currentTimeMillis())
        return random.nextInt(100000)
    }

    private suspend fun getItemsFromDb(limit: Int, offset: Int): List<LibraryItem> {
        val itemEntities = when (currentSortOrder) {
            SortOrder.BY_NAME -> libraryItemDao.getItemsSortedByName(limit, offset)
            SortOrder.BY_DATE -> libraryItemDao.getItemsSortedByDate(limit, offset)
        }

        return itemEntities.mapNotNull { entity ->
            when (entity.item) {
                "Book" -> bookDao.getBook(entity.id)?.toBook()
                "Newspaper" -> newspaperDao.getNewspaper(entity.id)?.toNewspaper()
                "Disk" -> diskDao.getDisk(entity.id)?.toDisk()
                else -> null
            }
        }
    }

    suspend fun addItem(item: LibraryItem): Result<Boolean> {
        return try {
            db.withTransaction {
                when (item) {
                    is Book -> {
                        val entity = item.toBookEntity()
                        libraryItemDao.insertItem(entity)
                        bookDao.insertBook(entity)
                        Library.getInstance().libraryItems.add(item)
                    }
                    is Newspaper -> {
                        val entity = item.toNewspaperEntity()
                        libraryItemDao.insertItem(entity)
                        newspaperDao.insertNewspaper(entity)
                        Library.getInstance().libraryItems.add(item)
                    }
                    is Disk -> {
                        val entity = item.toDiskEntity()
                        libraryItemDao.insertItem(entity)
                        diskDao.insertDisk(entity)
                        Library.getInstance().libraryItems.add(item)
                    }

                    else -> {}
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setSortOrder(order: SortOrder) {
        currentSortOrder = order
        currentOffset = 0
        loadedItems.clear()
    }

    private fun Book.toBookEntity() = BookEntity(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        pages = this.pages,
        author = this.author
    )

    private fun BookEntity.toBook() = Book(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        pages = this.pages,
        author = this.author
    )

    private fun Newspaper.toNewspaperEntity() = NewspaperEntity(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        month = this.month,
        issueNumber = this.issueNumber
    )

    private fun NewspaperEntity.toNewspaper() = Newspaper(
        id = this.id,
        name = this.name,
        month = this.month,
        isAvailable = this.isAvailable,
        issueNumber = this.issueNumber
    )

    private fun Disk.toDiskEntity() = DiskEntity(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        type = this.type
    )

    private fun DiskEntity.toDisk() = Disk(
        id = this.id,
        name = this.name,
        isAvailable = this.isAvailable,
        type = this.type
    )

    suspend fun getBookFromDb(id: Int): Book? = bookDao.getBook(id)?.toBook()
    suspend fun getNewspaperFromDb(id: Int): Newspaper? = newspaperDao.getNewspaper(id)?.toNewspaper()
    suspend fun getDiskFromDb(id: Int): Disk? = diskDao.getDisk(id)?.toDisk()
}