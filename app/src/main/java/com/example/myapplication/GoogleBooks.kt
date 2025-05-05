package com.example.myapplication

data class GoogleBooks (
    val books: List<ReceivedBook>?
)

data class ReceivedBook (
    val id: String?,
    val info: Info?
) {
    fun toBook(): Book {
        val identifier = info?.industryIdentifiers?.firstOrNull { it.type == "ISBN_10" }

        return Book(
            id = identifier.hashCode(),
            name = info?.title ?: "Без названия",
            isAvailable = true,
            pages = info?.pageCount ?: 0,
            author = info?.authors?.joinToString(", ") ?: "Неизвестный автор"
        )
    }
}

data class Info(
    val title: String?,
    val authors: List<String>?,
    val pageCount: Int?,
    val industryIdentifiers: List<Identifier>?
)

data class Identifier(
    val type: String,
    val identifier: String
)