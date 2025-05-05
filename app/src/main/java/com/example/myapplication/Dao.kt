package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LibraryItemDao {
    @Insert
    suspend fun insertItem(item: LibraryItemEntity): Long

    @Query("SELECT * FROM library_item ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getItemsSortedByName(limit: Int, offset: Int): List<LibraryItemEntity>

    @Query("SELECT * FROM library_item ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getItemsSortedByDate(limit: Int, offset: Int): List<LibraryItemEntity>

    @Query("SELECT COUNT(*) FROM library_item")
    suspend fun howMany(): Int
}

@Dao
interface BookDao {
    @Insert
    suspend fun insertBook(book: BookEntity): Long

    @Query("SELECT * FROM book WHERE id = :id")
    suspend fun getBook(id: Int): BookEntity?
}

@Dao
interface NewspaperDao {
    @Insert
    suspend fun insertNewspaper(newspaper: NewspaperEntity): Long

    @Query("SELECT * FROM newspaper WHERE id = :id")
    suspend fun getNewspaper(id: Int): NewspaperEntity?
}



@Dao
interface DiskDao {
    @Insert
    suspend fun insertDisk(disk: DiskEntity): Long

    @Query("SELECT * FROM disk WHERE id = :id")
    suspend fun getDisk(id: Int): DiskEntity?
}