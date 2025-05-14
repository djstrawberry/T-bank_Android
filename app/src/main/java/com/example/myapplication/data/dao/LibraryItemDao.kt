package com.example.myapplication.data.dao

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