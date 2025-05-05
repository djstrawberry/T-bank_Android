package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.myapplication.databinding.FragmentItemBinding
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
    private lateinit var library: Library
    private lateinit var repository: LibraryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isEditMode = it.getBoolean("IS_EDIT_MODE", false)
            currentItemId = it.getInt("ITEM_ID", -1)
            currentItemType = it.getString("ITEM_TYPE", "")
        }

        library = Library.getInstance()
        val db = GetDb.getDatabase(requireContext())
        repository = LibraryRepository(db)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentItemBinding.bind(view)

        if (isEditMode) {
            showTypeSelection()
            setupTypeSelectionButtons()
        } else {
            val itemId = arguments?.getInt("ITEM_ID", -1) ?: -1
            val itemType = arguments?.getString("ITEM_TYPE") ?: ""
            setupForView(itemId, itemType)
        }

        binding.btnSave.setOnClickListener {
            if (saveItem()) {
                if (isEditMode) {
                    (activity as? MainActivity)?.let { activity ->
                        activity.clearDetailFragment()
                        if (!activity.isLandscape) {
                            val libraryFragment = activity.supportFragmentManager
                                .findFragmentById(R.id.fragment_container_view1) as? LibraryFragment
                            libraryFragment?.scrollToLastItem()
                        }
                    }
                } else {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        if (isEditMode) {
            setupForCreate()
            return
        }

        binding.typeSelectionGroup.visibility = View.GONE
        binding.formContainer.visibility = View.VISIBLE

        currentItem = library.libraryItems.find { it.id == itemId && it.javaClass.simpleName == itemType }

        if (currentItem == null) {
            lifecycleScope.launch {
                try {
                    currentItem = withContext(Dispatchers.IO) {
                        when (itemType) {
                            "Book" -> repository.getBookFromDb(itemId)
                            "Newspaper" -> repository.getNewspaperFromDb(itemId)
                            "Disk" -> repository.getDiskFromDb(itemId)
                            else -> null
                        }?.also { newItem ->

                            library.libraryItems.add(newItem)
                        }
                    }

                    if (currentItem == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Элемент не найден", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        displayItem(currentItem!!)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            displayItem(currentItem!!)
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

        when(item) {
            is Book -> showBook(item)
            is Newspaper -> showNewspaper(item)
            is Disk -> showDisk(item)
        }
        binding.btnSave.visibility = View.GONE
    }

    private fun showBook(item: Book) {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Автор: "
            field1.setText(item.author)
            field1.isEnabled = false

            field2Layout.visibility = View.VISIBLE
            field2Label.text = "Страницы: "
            field2.setText(item.pages.toString())
            field2.isEnabled = false
        }
    }

    private fun showNewspaper(item: Newspaper) {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Месяц выпуска: "
            field1.setText(item.month)
            field1.isEnabled = false

            field2Layout.visibility = View.VISIBLE
            field2Label.text = "Номер выпуска: "
            field2.setText(item.issueNumber.toString())
            field2.isEnabled = false
        }
    }

    private fun showDisk(item: Disk) {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Тип диска: "
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
            field1Label.text = "Автор: "
            field1.setText("")
            field1.isEnabled = true

            field2Layout.visibility = View.VISIBLE
            field2Label.text = "Страницы: "
            field2.setText("")
            field2.isEnabled = true
        }
    }

    private fun createNewspaper() {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Месяц выпуска: "
            field1.setText("")
            field1.isEnabled = true

            field2Layout.visibility = View.VISIBLE
            field2Label.text = "Номер выпуска: "
            field2.setText("")
            field2.isEnabled = true
        }
    }

    private fun createDisk() {
        with(binding) {
            field1Layout.visibility = View.VISIBLE
            field1Label.text = "Тип диска: "
            field1.setText("")
            field1.isEnabled = true
            field2Layout.visibility = View.GONE
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
            Toast.makeText(requireContext(), "Введите название: ", Toast.LENGTH_SHORT).show()
            return false
        }

        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val nextId = withContext(Dispatchers.IO) {
                    repository.getNextAvailableId()
                }

                val newItem = when (selectedType) {
                    "Book" -> {
                        val author = binding.field1.text.toString().trim()
                        val pages = binding.field2.text.toString().toIntOrNull() ?: 0
                        if (author.isEmpty() || pages <= 0) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Заполните все поля корректно", Toast.LENGTH_SHORT).show()
                                binding.btnSave.isEnabled = true
                            }
                            return@launch
                        }
                        Book(nextId, name, true, pages, author)
                    }
                    "Newspaper" -> {
                        val month = binding.field1.text.toString().trim()
                        val issueNumber = binding.field2.text.toString().toIntOrNull() ?: 0
                        if (month.isEmpty() || issueNumber <= 0) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Заполните все поля корректно", Toast.LENGTH_SHORT).show()
                                binding.btnSave.isEnabled = true
                            }
                            return@launch
                        }
                        Newspaper(nextId, name, month, true, issueNumber)
                    }
                    "Disk" -> {
                        val type = binding.field1.text.toString().trim()
                        if (type.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Введите тип диска", Toast.LENGTH_SHORT).show()
                                binding.btnSave.isEnabled = true
                            }
                            return@launch
                        }
                        Disk(nextId, name, true, type)
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Неизвестный тип элемента", Toast.LENGTH_SHORT).show()
                            binding.btnSave.isEnabled = true
                        }
                        return@launch
                    }
                }

                val result = withContext(Dispatchers.IO) {
                    repository.addItem(newItem)
                }

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(requireContext(), "Элемент добавлен", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка: ${result.exceptionOrNull()?.message ?: "неизвестная ошибка"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.btnSave.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${e.message ?: "неизвестная ошибка"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnSave.isEnabled = true
                }
            }
        }
        return true
    }

    companion object {
        fun newInstance(
            isEditMode: Boolean = false,
            itemId: Int = -1,
            itemType: String = "",
            repository: LibraryRepository
        ) = ItemFragment().apply {
            arguments = Bundle().apply {
                putBoolean("IS_EDIT_MODE", isEditMode)
                putInt("ITEM_ID", itemId)
                putString("ITEM_TYPE", itemType)
            }
            this.repository = repository
        }
    }
}