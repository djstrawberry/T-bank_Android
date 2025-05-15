package com.example.myapplication.domain.models

sealed class LibraryItem {
    abstract val id: Int
    abstract val name: String
    abstract val isAvailable: Boolean
}