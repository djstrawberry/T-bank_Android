package com.example.myapplication.domain.models

data class Disk(
    override val id: Int,
    override val name: String,
    override val isAvailable: Boolean,
    val type: String
) : LibraryItem()