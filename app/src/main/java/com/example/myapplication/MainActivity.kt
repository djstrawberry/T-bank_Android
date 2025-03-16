package com.example.myapplication

fun main() {
    val library = Library()
    while (true) {
        println("Выберите действие:")
        println("1. Показать книги")
        println("2. Показать газеты")
        println("3. Показать диски")
        println("0. Выход")
        library.run {
            when (readlnOrNull()?.toIntOrNull()) {
                1 -> showItems("Book")
                2 -> showItems("Newspaper")
                3 -> showItems("Disk")
                0 -> return
                else -> println("Неправильный выбор. Попробуйте еще раз.")
            }
        }
    }
}


open class LibraryItem(
    val id: Int,
    val name: String,
    var isAvailable: Boolean
) {
    open fun getShortInfo(): String {
        return "$name доступна: ${if (isAvailable) "Да" else "Нет"}"
    }

    open fun getDetailedInfo(): String {
        return getShortInfo()
    }
}

class Book(
    id: Int,
    name: String,
    isAvailable: Boolean,
    val pages: Int,
    val author: String
) : LibraryItem(id, name, isAvailable) {
    override fun getDetailedInfo(): String {
        return "книга: $name ($pages стр.) автора: $author с id: $id доступна: ${if (isAvailable) "Да" else "Нет"}"
    }
}

class Newspaper(
    id: Int,
    name: String,
    isAvailable: Boolean,
    val issueNumber: Int
) : LibraryItem(id, name, isAvailable) {
    override fun getDetailedInfo(): String {
        return "выпуск: $issueNumber газеты $name с id: $id доступен: ${if (isAvailable) "Да" else "Нет"}"
    }
}

class Disk(
    id: Int,
    name: String,
    isAvailable: Boolean,
    val type: String
) : LibraryItem(id, name, isAvailable) {
    override fun getDetailedInfo(): String {
        return "$type $name доступен: ${if (isAvailable) "Да" else "Нет"}"
    }
}

class Library {
    val libraryItems = listOf(
        Book(1, "Солярис", true, 288, "Станислав Лем"),
        Book(2, "Дюна", false, 1504, "Фрэнк Герберт"),
        Newspaper(3, "Непоседа", false, 777),
        Disk(4, "Артур и минипуты", true, "DVD"),
        Disk(5, "Ария", true, "CD")
    )

    fun showItems(whatIsIt: String) {
        var i: Int = 1
        val neededItems = when (whatIsIt) {
            "Book" -> libraryItems.filterIsInstance<Book>()
            "Newspaper" -> libraryItems.filterIsInstance<Newspaper>()
            else -> libraryItems.filterIsInstance<Disk>()
        }

        neededItems.forEach { item ->
            println("${i++}. ${item.getShortInfo()}")
        }

        println("Выберите объект по номеру:")
        val selectedIndex = readlnOrNull()?.toIntOrNull() ?: -1

        if ((selectedIndex - 1) !in neededItems.indices) {
            println("Неверный выбор.")
            return
        }

        val selectedItem = neededItems[selectedIndex - 1]
        handleAction(selectedItem)
    }

    fun handleAction(item: LibraryItem) {
        while (true) {
            println("Выберите действие:")
            println("1. Взять домой")
            println("2. Читать в читальном зале")
            println("3. Показать подробную информацию")
            println("4. Вернуть")
            println("0. Назад")

            when (readlnOrNull()?.toIntOrNull()) {
                1 -> takeHome(item)
                2 -> readInLibrary(item)
                3 -> println(item.getDetailedInfo())
                4 -> returnItem(item)
                0 -> return
                else -> println("Неверный ввод. Попробуйте снова.")
            }
        }
    }

    fun takeHome(item: LibraryItem) {
        when (item) {
            is Book, is Disk -> {
                if (item.isAvailable) {
                    item.isAvailable = false
                    println("Предмет с id ${item.id} взяли домой.")
                } else {
                    println("Предмет недоступен для взятия домой.")
                }
            }
            else -> println("Газеты нельзя брать домой.")
        }
    }

    fun readInLibrary(item: LibraryItem) {
        when (item) {
            is Book, is Newspaper -> {
                if (item.isAvailable) {
                    item.isAvailable = false
                    println("Предмет с id ${item.id} взяли в читальный зал.")
                } else {
                    println("Предмет недоступен для чтения в зале.")
                }
            }
            else -> println("Диски нельзя читать в читальном зале.")
        }
    }

    fun returnItem(item: LibraryItem) {
        if (!item.isAvailable) {
            item.isAvailable = true
            println("Предмет с id ${item.id} вернули.")
        } else {
            println("Предмет уже доступен.")
        }
    }
}