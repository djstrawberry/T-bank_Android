package com.example.myapplication.domain.models

data class Newspaper(
    override val id: Int,
    override val name: String,
    override val isAvailable: Boolean,
    val month: String,
    val issueNumber: Int
) : LibraryItem()