package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookDao {
    @Insert
    suspend fun insertBook(book: BookEntity): Long

    @Query("SELECT * FROM book WHERE id = :id")
    suspend fun getBook(id: Int): BookEntity?
}