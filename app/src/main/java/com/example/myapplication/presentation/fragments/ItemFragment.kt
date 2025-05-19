package com.example.myapplication.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentItemBinding
import com.example.myapplication.domain.models.Book
import com.example.myapplication.domain.models.Disk
import com.example.myapplication.domain.models.LibraryItem
import com.example.myapplication.domain.models.Newspaper
import com.example.myapplication.domain.repositories.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemFragment : Fragment(R.layout.fragment_item) {
    private var _binding: FragmentItemBinding? = null
    private val binding get() = _binding!!
    private var currentItem: LibraryItem? = null
    private var currentItemId: Int = -1
    private var currentItemType: String = ""
    private var isEditMode: Boolean = false
    private var selectedType: String = "Book"
    private lateinit var repository: LibraryRepository

    companion object {
        private const val ARG_IS_EDIT_MODE = "IS_EDIT_MODE"
        private const val ARG_ITEM_ID = "ITEM_ID"
        private const val ARG_ITEM_TYPE = "ITEM_TYPE"

        fun newInstance(
            isEditMode: Boolean = false,
            itemId: Int = -1,
            itemType: String = "",
        ): ItemFragment {
            return ItemFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_EDIT_MODE, isEditMode)
                    putInt(ARG_ITEM_ID, itemId)
                    putString(ARG_ITEM_TYPE, itemType)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.let {
            isEditMode = it.getBoolean(ARG_IS_EDIT_MODE, false)
            currentItemId = it.getInt(ARG_ITEM_ID, -1)
            currentItemType = it.getString(ARG_ITEM_TYPE, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentItemBinding.bind(view)

        if (isEditMode) {
            showTypeSelection()
            setupTypeSelectionButtons()
        } else {
            setupForView(currentItemId, currentItemType)
        }

        binding.btnSave.setOnClickListener {
            if (saveItem()) {
                requireActivity().onBackPressed()
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

        lifecycleScope.launch {
            try {
                currentItem = withContext(Dispatchers.IO) {
                    when (itemType) {
                        "Book" -> repository.getBook(itemId)
                        "Newspaper" -> repository.getNewspaper(itemId)
                        "Disk" -> repository.getDisk(itemId)
                        else -> null
                    }
                }
                currentItem?.let { displayItem(it) }
                    ?: showError("Элемент не найден")
            } catch (e: Exception) {
                showError("Ошибка загрузки: ${e.message}")
            }
        }
    }

    private fun displayItem(item: LibraryItem) {
        binding.itemName.setText(item.name)
        binding.itemName.isEnabled = false

        binding.itemIcon.setImageResource(when(item) {
            is Book -> R.drawable.ic_book
            is Newspaper -> R.drawable.ic_newspaper
            is Disk -> R.drawable.ic_disk
            else -> R.drawable.ic_default
        })

        when (item) {
            is Book -> showBook(item)
            is Newspaper -> showNewspaper(item)
            is Disk -> showDisk(item)
        }
        binding.btnSave.visibility = View.GONE
    }

    private fun showBook(item: Book) {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Автор:"
            field1.setText(item.author)
            field1.isEnabled = false

            field2Layout.visibility = View.VISIBLE
            field2Label.text = "Страницы:"
            field2.setText(item.pages.toString())
            field2.isEnabled = false
        }
    }

    private fun showNewspaper(item: Newspaper) {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Месяц выпуска:"
            field1.setText(item.month)
            field1.isEnabled = false

            field2Layout.visibility = View.VISIBLE
            field2Label.text = "Номер выпуска:"
            field2.setText(item.issueNumber.toString())
            field2.isEnabled = false
        }
    }

    private fun showDisk(item: Disk) {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Тип диска:"
            field1.setText(item.type)
            field1.isEnabled = false
            field2Layout.visibility = View.GONE
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

        when (selectedType) {
            "Book" -> createBook()
            "Newspaper" -> createNewspaper()
            "Disk" -> createDisk()
        }
    }

    private fun createBook() {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Автор:"
            field1.setText("")
            field1.isEnabled = true

            field2Layout.visibility = View.VISIBLE
            field2Label.text = "Страницы:"
            field2.setText("")
            field2.isEnabled = true
        }
    }

    private fun createNewspaper() {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Месяц выпуска:"
            field1.setText("")
            field1.isEnabled = true

            field2Layout.visibility = View.VISIBLE
            field2Label.text = "Номер выпуска:"
            field2.setText("")
            field2.isEnabled = true
        }
    }

    private fun createDisk() {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Тип диска:"
            field1.setText("")
            field1.isEnabled = true
            field2Layout.visibility = View.GONE
        }
    }

    private fun saveItem(): Boolean {
        return if (isEditMode) {
            createNewItem()
        } else {
            true
        }
    }

    private fun createNewItem(): Boolean {
        val name = binding.itemName.text?.toString()?.trim() ?: run {
            showError("Введите название")
            return false
        }

        lifecycleScope.launch {
            try {
                val newItem = when (selectedType) {
                    "Book" -> createBookItem(name)
                    "Newspaper" -> createNewspaperItem(name)
                    "Disk" -> createDiskItem(name)
                    else -> null
                } ?: return@launch

                val result = withContext(Dispatchers.IO) {
                    repository.addItem(newItem)
                }

                if (isAdded) {
                    if (result.isSuccess) {
                        requireActivity().onBackPressed()
                    } else {
                        showError("Ошибка сохранения: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                if (isAdded) showError("Ошибка: ${e.message}")
            }
        }
        return true
    }

    private suspend fun createBookItem(name: String): Book? {
        val author = binding.field1.text.toString().trim()
        val pages = binding.field2.text.toString().toIntOrNull() ?: 0
        if (author.isEmpty() || pages <= 0) {
            showError("Заполните все поля корректно")
            return null
        }
        return Book(
            id = repository.getNextAvailableId(),
            name = name,
            isAvailable = true,
            pages = pages,
            author = author
        )
    }

    private suspend fun createNewspaperItem(name: String): Newspaper? {
        val month = binding.field1.text.toString().trim()
        val issueNumber = binding.field2.text.toString().toIntOrNull() ?: 0

        if (month.isEmpty() || issueNumber <= 0) {
            showError("Заполните все поля корректно")
            return null
        }

        return Newspaper(
            id = repository.getNextAvailableId(),
            name = name,
            isAvailable = true,
            month = month,
            issueNumber = issueNumber
        )
    }

    private suspend fun createDiskItem(name: String): Disk? {
        val type = binding.field1.text.toString().trim()

        if (type.isEmpty()) {
            showError("Укажите тип диска")
            return null
        }

        return Disk(
            id = repository.getNextAvailableId(),
            name = name,
            isAvailable = true,
            type = type
        )
    }

    private fun handleSaveResult(result: Result<Boolean>) {
        if (result.isSuccess) {
            showMessage("Элемент сохранён")
            requireActivity().onBackPressed()
        } else {
            showError("Ошибка сохранения: ${result.exceptionOrNull()?.message}")
        }
    }

    private fun showError(message: String) {
        if (!isAdded || context == null) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showMessage(message: String) {
        if (!isAdded || context == null) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}