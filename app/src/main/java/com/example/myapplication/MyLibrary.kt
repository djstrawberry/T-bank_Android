package com.example.myapplication

fun main() {
    val library = Library()
    val manager = Manager()
    var checkExit = true
    while (checkExit) {
        println("Выберите действие:")
        println("1. Показать книги")
        println("2. Показать газеты")
        println("3. Показать диски")
        println("4. Купить объект")
        println("0. Выход")
        library.run {
            when (readlnOrNull()?.toIntOrNull()) {
                1 -> showItems("Book")
                2 -> showItems("Newspaper")
                3 -> showItems("Disk")
                4 -> manager.runManager()
                0 -> checkExit = false
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
    private val pages: Int,
    private val author: String
) : LibraryItem(id, name, isAvailable) {
    override fun getDetailedInfo(): String {
        return "книга: $name ($pages стр.) автора: $author с id: $id доступна: ${if (isAvailable) "Да" else "Нет"}"
    }
}

class Newspaper(
    id: Int,
    name: String,
    private val month: String,
    isAvailable: Boolean,
    private val issueNumber: Int
) : LibraryItem(id, name, isAvailable) {
    override fun getDetailedInfo(): String {
        return "выпуск: $issueNumber газеты $name месяц выпуска: $month с id: $id доступен: ${if (isAvailable) "Да" else "Нет"}"
    }
}

class Disk(
    id: Int,
    name: String,
    isAvailable: Boolean,
    private val type: String
) : LibraryItem(id, name, isAvailable) {
    override fun getDetailedInfo(): String {
        return "$type $name доступен: ${if (isAvailable) "Да" else "Нет"}"
    }
}

 // сама библиотека
 open class Library {
    val libraryItems = listOf(
        Book(23423, "Солярис", true, 288, "Станислав Лем"),
        Book(456, "Дюна", false, 1504, "Фрэнк Герберт"),
        Newspaper(12, "The New York Times", "Декабрь",false, 777),
        Disk(568, "Война миров", true, "DVD"),
        Disk(5, "Ария", true, "CD")
    )

    fun showItems(whatIsIt: String) {
        var counterOfNeeded: Int = 1
        val neededItems = when (whatIsIt) {
            "Book" -> libraryItems.filterIsInstance<Book>()
            "Newspaper" -> libraryItems.filterIsInstance<Newspaper>()
            else -> libraryItems.filterIsInstance<Disk>()
        }

        neededItems.forEach { item ->
            println("${counterOfNeeded++}. ${item.getShortInfo()}")
        }

        println("Выберите объект по номеру или введите 0 для возвращения назад:")
        val selectedIndex = readlnOrNull()?.toIntOrNull() ?: -1

        if ((selectedIndex - 1) !in neededItems.indices && selectedIndex != 0) {
            println("Неверный выбор.")
            return
        }
        if (selectedIndex == 0)
            return

        val selectedItem = neededItems[selectedIndex - 1]
        handleAction(selectedItem)
    }

    fun handleAction(item: LibraryItem) {
        val digitizationOffice = DigitizationOffice()
        var checkExit = true
        while (checkExit) {
            println("Выберите действие:")
            println("1. Взять домой")
            println("2. Читать в читальном зале")
            println("3. Показать подробную информацию")
            println("4. Вернуть")
            println("5. Оцифровать")
            println("0. Назад")

            when (readlnOrNull()?.toIntOrNull()) {
                1 -> takeHome(item)
                2 -> readInLibrary(item)
                3 -> println(item.getDetailedInfo())
                4 -> returnItem(item)
                5 -> digitizationOffice.digitize(item)
                0 -> checkExit = false
                else -> println("Неверный ввод. Попробуйте снова.")
            }
        }
    }

    private fun takeHome(item: LibraryItem) {
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

    private fun readInLibrary(item: LibraryItem) {
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

    private fun returnItem(item: LibraryItem) {
        if (!item.isAvailable) {
            item.isAvailable = true
            println("Предмет с id ${item.id} вернули.")
        } else {
            println("Предмет уже доступен.")
        }
    }
}

// магазин с менеджером
interface Store<T: LibraryItem> {
    fun sell(): T
}

class BookStore: Store<Book> {
    override fun sell(): Book {
        return Book(666, "Очень интересная книга", true, 1000, "Хороший автор")
    }
}

class NewspaperStore: Store<Newspaper> {
    override fun sell(): Newspaper {
        return Newspaper(666, "Очень интересная газета", "Май", true, 111)
    }
}

class DiskStore: Store<Disk> {
    override fun sell(): Disk {
        return Disk(666, "Очень интересный диск", true, "CD")
    }
}

class Manager {
    fun <T: LibraryItem> buy(store: Store<T>): T {
        return store.sell()
    }

    fun runManager() {
        println("Выберите тип объекта для покупки:")
        println("1. Книга")
        println("2. Газета")
        println("3. Диск")

        when (readlnOrNull()?.toIntOrNull()) {
            1 -> {
                val newBook = buy(BookStore())
                println("Куплена книга: ${newBook.getDetailedInfo()}")
            }

            2 -> {
                val newNewspaper = buy(NewspaperStore())
                println("Куплена газета: ${newNewspaper.getDetailedInfo()}")
            }

            3 -> {
                val newDisk = buy(DiskStore())
                println("Куплен диск: ${newDisk.getDetailedInfo()}")
            }

            else -> println("Неправильный выбор. Попробуйте еще раз.")
        }
    }
}

// кабинет оцифровки
class DigitizationOffice {
    fun digitize (item: LibraryItem): Disk {
        println("Предмет с id ${item.id} оцифрован.")
        return Disk(item.id, "Оцифрованный предмет: ${item.name}", true, "CD")
    }
}

// inline функция с refied
inline fun <reified T> filterByType(items: List<LibraryItem>): List<T> {
    return items.filterIsInstance<T>()
}
