package com.example.myapplication.data.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "library_item")
open class LibraryItemEntity(
    @PrimaryKey
    val id: Int,
    var name: String,
    var isAvailable: Boolean,
    var item: String,
    var createdAt: Date = Date()
)

@Entity(tableName = "book")
class BookEntity(
    id: Int,
    name: String,
    isAvailable: Boolean,
    var pages: Int,
    var author: String,
    createdAt: Date = Date()
) : LibraryItemEntity(id, name, isAvailable, "Book", createdAt)

@Entity(tableName = "newspaper")
class NewspaperEntity(
    id: Int,
    name: String,
    isAvailable: Boolean,
    var month: String,
    var issueNumber: Int,
    createdAt: Date = Date()
) : LibraryItemEntity(id, name, isAvailable, "Newspaper", createdAt)

@Entity(tableName = "disk")
class DiskEntity(
    id: Int,
    name: String,
    isAvailable: Boolean,
    var type: String,
    createdAt: Date = Date()
) : LibraryItemEntity(id, name, isAvailable, "Disk", createdAt)