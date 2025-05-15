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
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment(R.layout.fragment_library) {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LibraryAdapter
    private lateinit var libraryRepository: LibraryRepository
    private lateinit var googleBooksRepository: GoogleBooksRepository

    private val authorEditText: TextInputEditText by lazy {
        binding.etAuthor.findViewById(R.id.etAuthor)
    }

    private val titleEditText: TextInputEditText by lazy {
        binding.etTitle.findViewById(R.id.etTitle)
    }

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

        val app = requireActivity().application as MyApplication
        libraryRepository = app.libraryRepository
        googleBooksRepository = app.googleBooksRepository

        setupRecyclerView()
        setupSortingButtons()
        setupModeSwitcher()
        loadItems()

        binding.fab.setOnClickListener {
            (activity as? MainActivity)?.showCreateItem()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.btnSearch.isEnabled =
                    binding.etAuthor.text?.length!! >= 3 ||
                            binding.etTitle.text?.length!! >= 3
            }
        }

        binding.etAuthor.addTextChangedListener(textWatcher)
        binding.etTitle.addTextChangedListener(textWatcher)
    }

    private fun setupModeSwitcher() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Библиотека"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Google Books"))


        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    MODE_LIBRARY -> {
                        currentMode = MODE_LIBRARY
                        val params = binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams
                        params.topToBottom = R.id.sortButtonsLayout
                        binding.recyclerView.layoutParams = params
                        loadItems()
                        binding.searchContainer.visibility = View.GONE
                        binding.sortButtonsLayout.visibility = View.VISIBLE
                        binding.fab.visibility = View.VISIBLE
                    }
                    MODE_GOOGLE_BOOKS -> {
                        currentMode = MODE_GOOGLE_BOOKS
                        val params = binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams
                        params.topToBottom = R.id.searchContainer
                        binding.recyclerView.layoutParams = params
                        adapter.updateItems(emptyList())
                        updateSearchButtonState()
                        binding.searchContainer.visibility = View.VISIBLE
                        binding.sortButtonsLayout.visibility = View.GONE
                        binding.fab.visibility = View.GONE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        updateSearchButtonState()
        binding.btnSearch.setOnClickListener {
            if (binding.btnSearch.isEnabled) {
                searchGoogleBooks()
            }
        }
    }

    private fun updateSearchButtonState() {
        val authorText = authorEditText.text?.toString()?.trim()
        val titleText = titleEditText.text?.toString()?.trim()

        binding.btnSearch.isEnabled = !authorText.isNullOrEmpty() && authorText.length >= 3 ||
                !titleText.isNullOrEmpty() && titleText.length >= 3
    }

    private fun searchGoogleBooks() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                adapter.updateItems(emptyList())

                val author = authorEditText.text?.toString()?.takeIf { it.isNotBlank() }
                val title = titleEditText.text?.toString()?.takeIf { it.isNotBlank() }

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

    private fun setupRecyclerView() {
        adapter = LibraryAdapter(emptyList()).apply {
            onItemClick = { item ->
                if (currentMode == MODE_LIBRARY) {
                    (activity as? MainActivity)?.showItemDetails(item.id, item::class.simpleName ?: "")
                }
            }
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

    private fun setupSortingButtons() {
        binding.sortByName.setOnClickListener {
            lifecycleScope.launch {
                libraryRepository.setSortOrder(LibraryRepository.SortOrder.BY_NAME)
                loadItems()
            }
        }

        binding.sortByDate.setOnClickListener {
            lifecycleScope.launch {
                libraryRepository.setSortOrder(LibraryRepository.SortOrder.BY_DATE)
                loadItems()
            }
        }
    }

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