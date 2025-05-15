package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NewspaperDao {
    @Insert
    suspend fun insertNewspaper(newspaper: NewspaperEntity): Long

    @Query("SELECT * FROM newspaper WHERE id = :id")
    suspend fun getNewspaper(id: Int): NewspaperEntity?
}