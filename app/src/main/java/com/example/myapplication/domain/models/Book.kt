package com.example.myapplication.domain.models

data class Book(
    override val id: Int,
    override val name: String,
    override val isAvailable: Boolean,
    val pages: Int,
    val author: String
) : LibraryItem()