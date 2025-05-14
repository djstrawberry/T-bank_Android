package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DiskDao {
    @Insert
    suspend fun insertDisk(disk: DiskEntity): Long

    @Query("SELECT * FROM disk WHERE id = :id")
    suspend fun getDisk(id: Int): DiskEntity?
}