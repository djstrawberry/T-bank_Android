package com.example.myapplication

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [LibraryItemEntity::class, BookEntity::class, NewspaperEntity::class, DiskEntity::class],
    version = 1,
)
@TypeConverters(DateConverter::class)
abstract class DatabaseLibrary : RoomDatabase() {
    abstract fun libraryItemDao(): LibraryItemDao
    abstract fun bookDao(): BookDao
    abstract fun newspaperDao(): NewspaperDao
    abstract fun diskDao(): DiskDao

    companion object {
        const val DATABASE_NAME = "library_db"
    }
}