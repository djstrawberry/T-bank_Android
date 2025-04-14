package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityItemBinding

class ItemActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityItemBinding.inflate(layoutInflater)
    }
    private var currentItem: LibraryItem? = null
    private var isEditMode: Boolean = false
    private var selectedType: String = "Book"
    private lateinit var library: Library

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        library = Library.getInstance()

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)

        if (isEditMode) {
            showTypeSelection()
            setupTypeSelectionButtons()
        } else {
            val itemId = intent.getIntExtra("ITEM_ID", -1)
            val itemType = intent.getStringExtra("ITEM_TYPE") ?: ""
            setupForView(itemId, itemType)
        }

        binding.btnSave.setOnClickListener {
            if (saveItem()) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun setupTypeSelectionButtons() {
        binding.btnSelectBook.setOnClickListener { selectType("Book") }
        binding.btnSelectNewspaper.setOnClickListener { selectType("Newspaper") }
        binding.btnSelectDisk.setOnClickListener { selectType("Disk") }
    }

    private fun showTypeSelection() {
        binding.typeSelectionGroup.visibility = View.VISIBLE
        binding.formContainer.visibility = View.GONE
    }

    private fun selectType(type: String) {
        selectedType = type
        setupForCreate()
    }

    private fun setupForView(itemId: Int, itemType: String) {
        binding.typeSelectionGroup.visibility = View.GONE
        binding.formContainer.visibility = View.VISIBLE

        currentItem = library.libraryItems.find { it.id == itemId && it.javaClass.simpleName == itemType }

        currentItem?.let { item ->
            binding.itemName.setText(item.name)
            binding.itemName.isEnabled = false

            binding.itemIcon.setImageResource(when(item) {
                is Book -> R.drawable.ic_book
                is Newspaper -> R.drawable.ic_newspaper
                is Disk -> R.drawable.ic_disk
                else -> R.drawable.ic_default
            })

            with(binding) {
            when(item) {
                is Book -> {
                    field1Layout.visibility = View.VISIBLE
                    field1Label.text = "Автор: "
                    field1.setText(item.author)
                    field1.isEnabled = false

                    field2Layout.visibility = View.VISIBLE
                    field2Label.text = "Страницы: "
                    field2.setText(item.pages.toString())
                    field2.isEnabled = false
                }

                is Newspaper -> {
                    field1Layout.visibility = View.VISIBLE
                    field1Label.text = "Месяц выпуска: "
                    field1.setText(item.month)
                    field1.isEnabled = false

                    field2Layout.visibility = View.VISIBLE
                    field2Label.text = "Номер выпуска: "
                    field2.setText(item.issueNumber.toString())
                    field2.isEnabled = false
                }

                is Disk -> {
                    field1Layout.visibility = View.VISIBLE
                    field1Label.text = "Тип диска: "
                    field1.setText(item.type)
                    field1.isEnabled = false
                    field2Layout.visibility = View.GONE
                }
            }
            }
        } ?: run {
            Toast.makeText(this, "Элемент не найден.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupForCreate() {
        binding.typeSelectionGroup.visibility = View.GONE
        binding.formContainer.visibility = View.VISIBLE

        binding.itemName.setText("")
        binding.itemName.isEnabled = true

        binding.itemIcon.setImageResource(when(selectedType) {
            "Book" -> R.drawable.ic_book
            "Newspaper" -> R.drawable.ic_newspaper
            "Disk" -> R.drawable.ic_disk
            else -> R.drawable.ic_default
        })
        with(binding) {
            when (selectedType) {
                "Book" -> {
                    field1Layout.visibility = View.VISIBLE
                    field1Label.text = "Автор: "
                    field1.setText("")
                    field1.isEnabled = true

                    field2Layout.visibility = View.VISIBLE
                    field2Label.text = "Страницы: "
                    field2.setText("")
                    field2.isEnabled = true
                }

                "Newspaper" -> {
                    field1Layout.visibility = View.VISIBLE
                    field1Label.text = "Месяц выпуска: "
                    field1.setText("")
                    field1.isEnabled = true

                    field2Layout.visibility = View.VISIBLE
                    field2Label.text = "Номер выпуска: "
                    field2.setText("")
                    field2.isEnabled = true
                }

                "Disk" -> {
                    field1Layout.visibility = View.VISIBLE
                    field1Label.text = "Тип диска: "
                    field1.setText("")
                    field1.isEnabled = true
                    field2Layout.visibility = View.GONE
                }
            }
        }
    }

    private fun saveItem(): Boolean {
        if (isEditMode) {
            return createNewItem()
        }
        return true
    }
    private fun createNewItem(): Boolean {
        val name = binding.itemName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название: ", Toast.LENGTH_SHORT).show()
            return false
        }

        val newId = (library.libraryItems.maxOfOrNull { it.id } ?: 0) + 1

        val newItem = when (selectedType) {
            "Book" -> {
                val author = binding.field1.text.toString().trim()
                val pages = binding.field2.text.toString().toIntOrNull() ?: 0
                if (author.isEmpty() || pages <= 0) {
                    Toast.makeText(this, "Заполните все поля корректно.", Toast.LENGTH_SHORT).show()
                    return false
                }
                Book(newId, name, true, pages, author)
            }
            "Newspaper" -> {
                val month = binding.field1.text.toString().trim()
                val issueNumber = binding.field2.text.toString().toIntOrNull() ?: 0
                if (month.isEmpty() || issueNumber <= 0) {
                    Toast.makeText(this, "Заполните все поля корректно.", Toast.LENGTH_SHORT).show()
                    return false
                }
                Newspaper(newId, name, month, true, issueNumber)
            }
            "Disk" -> {
                val type = binding.field1.text.toString().trim()
                if (type.isEmpty()) {
                    Toast.makeText(this, "Введите тип диска: ", Toast.LENGTH_SHORT).show()
                    return false
                }
                Disk(newId, name, true, type)
            }
            else -> {
                Toast.makeText(this, "Неизвестный тип элемента.", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        library.libraryItems.add(newItem)
        Toast.makeText(this, "Элемент добавлен.", Toast.LENGTH_SHORT).show()
        return true
    }
}