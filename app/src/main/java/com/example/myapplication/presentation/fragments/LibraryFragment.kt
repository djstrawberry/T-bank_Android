package com.example.myapplication.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.common.LibraryAdapter
import com.example.myapplication.databinding.FragmentLibraryBinding
import com.example.myapplication.domain.models.LibraryItem
import com.example.myapplication.domain.repositories.GoogleBooksRepository
import com.example.myapplication.domain.repositories.LibraryRepository
import com.example.myapplication.presentation.MyApplication
import com.example.myapplication.presentation.activities.MainActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment(R.layout.fragment_library) {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LibraryAdapter
    private lateinit var libraryRepository: LibraryRepository
    private lateinit var googleBooksRepository: GoogleBooksRepository

    // Режимы работы: 0 = Библиотека, 1 = Google Books
    private companion object {
        const val MODE_LIBRARY = 0
        const val MODE_GOOGLE_BOOKS = 1
    }

    private var currentMode = MODE_LIBRARY

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        val params = binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = R.id.sortButtonsLayout
        binding.recyclerView.layoutParams = params

        // Получаем репозитории из Application
        val app = requireActivity().application as MyApplication
        libraryRepository = app.libraryRepository
        googleBooksRepository = app.googleBooksRepository

        setupRecyclerView() // Настраиваем список
        setupSortingButtons() // Кнопки сортировки
        setupModeSwitcher() // Вкладки переключения режимов
        loadItems() // Загружаем данные

        // Кнопка добавления нового элемента
        binding.fab.setOnClickListener {
            (activity as? MainActivity)?.showCreateItem()
        }
    }

    // Настройка вкладок (табов)
    private fun setupModeSwitcher() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Библиотека"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Google Books"))

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSearchButtonState()
            }
        }

        // Обработчик переключения вкладок
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    MODE_LIBRARY -> { // Режим "Библиотека"
                        currentMode = MODE_LIBRARY
                        val params = binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams
                        params.topToBottom = R.id.sortButtonsLayout
                        binding.recyclerView.layoutParams = params
                        loadItems()
                        binding.searchContainer.visibility = View.GONE // Скрываем поиск
                        binding.sortButtonsLayout.visibility = View.VISIBLE // Показываем сортировку
                        binding.fab.visibility = View.VISIBLE // Показываем кнопку добавления
                    }
                    MODE_GOOGLE_BOOKS -> { // Режим "Google Books"
                        currentMode = MODE_GOOGLE_BOOKS
                        val params = binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams
                        params.topToBottom = R.id.searchContainer
                        binding.recyclerView.layoutParams = params
                        adapter.updateItems(emptyList())
                        updateSearchButtonState()
                        binding.searchContainer.visibility = View.VISIBLE // Показываем поиск
                        binding.sortButtonsLayout.visibility = View.GONE // Скрываем сортировку
                        binding.fab.visibility = View.GONE // Скрываем кнопку добавления
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Кнопка поиска в Google Books
        binding.btnSearch.setOnClickListener {
            if (binding.btnSearch.isEnabled) {
                searchGoogleBooks()
            }
        }
    }

    private fun updateSearchButtonState() {
        val authorText = binding.etAuthor.text?.toString()?.trim()
        val titleText = binding.etTitle.text?.toString()?.trim()

        // Кнопка активна, если хотя бы одно поле содержит 3 или более символа
        binding.btnSearch.isEnabled = !authorText.isNullOrEmpty() && authorText.length >= 3 ||
                !titleText.isNullOrEmpty() && titleText.length >= 3
    }

    // Поиск книг в Google Books
    private fun searchGoogleBooks() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val author = binding.etAuthor.text?.toString()?.takeIf { it.isNotBlank() }
                val title = binding.etTitle.text?.toString()?.takeIf { it.isNotBlank() }

                val books = withContext(Dispatchers.IO) {
                    googleBooksRepository.searchBooks(author, title)
                }

                if (books.isEmpty()) {
                    Toast.makeText(requireContext(), "Книги не найдены", Toast.LENGTH_SHORT).show()
                } else {
                    adapter.updateItems(books)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка поиска: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // Настройка списка (RecyclerView)
    private fun setupRecyclerView() {
        adapter = LibraryAdapter(emptyList()).apply {
            // Клик по элементу
            onItemClick = { item ->
                if (currentMode == MODE_LIBRARY) {
                    (activity as? MainActivity)?.showItemDetails(item.id, item::class.simpleName ?: "")
                }
            }
            // Долгий клик (для сохранения из Google Books в библиотеку)
            onItemLongClick = { item ->
                if (currentMode == MODE_GOOGLE_BOOKS) {
                    saveBookToLibrary(item)
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LibraryFragment.adapter
        }
    }

    // Сохранение книги из Google Books в локальную библиотеку
    private fun saveBookToLibrary(item: LibraryItem) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    libraryRepository.addItem(item)
                }

                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Книга добавлена в библиотеку", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Настройка кнопок сортировки
    private fun setupSortingButtons() {
        // Сортировка по имени
        binding.sortByName.setOnClickListener {
            lifecycleScope.launch {
                libraryRepository.setSortOrder(LibraryRepository.SortOrder.BY_NAME)
                loadItems()
            }
        }

        // Сортировка по дате
        binding.sortByDate.setOnClickListener {
            lifecycleScope.launch {
                libraryRepository.setSortOrder(LibraryRepository.SortOrder.BY_DATE)
                loadItems()
            }
        }
    }

    // Загрузка элементов
    private fun loadItems() {
        lifecycleScope.launch {
            if (currentMode == MODE_LIBRARY) {
                val result = withContext(Dispatchers.IO) {
                    libraryRepository.getLibraryItems(false)
                }

                result.onSuccess { items ->
                    adapter.updateItems(items)
                }.onFailure {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}