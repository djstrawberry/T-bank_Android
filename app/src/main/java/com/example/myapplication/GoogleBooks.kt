package com.example.myapplication

data class GoogleBooksResponse (
    val items: List<ReceivedBook>?
)

data class ReceivedBook (
    val id: String?,
    val volumeInfo: VolumeInfo?
) {
    fun toBook(): Book {
        val identifier = volumeInfo?.industryIdentifiers?.firstOrNull { it.type == "ISBN_10" }
        return Book(
            id = identifier.hashCode(),
            name = volumeInfo?.title ?: "Без названия",
            isAvailable = true,
            pages = volumeInfo?.pageCount ?: 0,
            author = volumeInfo?.authors?.joinToString(", ") ?: "Неизвестный автор"
        )
    }
}

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val pageCount: Int?,
    val industryIdentifiers: List<Identifier>?
)

data class Identifier(
    val type: String,
    val identifier: String
)