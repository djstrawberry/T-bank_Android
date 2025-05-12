package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentLibraryBinding
import com.example.myapplication.presentation.activities.MainActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private companion object {
        const val VISIBLE_ITEMS = 5
        const val MODE_LIBRARY = 0
        const val MODE_GOOGLE_BOOKS = 1
    }

    private var currentMode = MODE_LIBRARY
    private lateinit var googleBooksRepository: GoogleBooksRepository
    private lateinit var repository: LibraryRepository
    private var googleBooksSearchJob: Job? = null
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LibraryAdapter
    private var job: Job? = null
    private var isLoading = false
    private var isFirstLoad = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        val db = GetDb.getDatabase(requireContext())

        googleBooksRepository = GoogleBooksRepository(requireContext())
        repository = LibraryRepository(db)

        setupRecyclerView(repository)
        setupSortingButtons(repository)
        setupModeSwitcher()
        loadLibraryItems(repository, initialLoad = true)

        adapter.onItemClick = { item ->
            (activity as? MainActivity)?.showItemDetails(item.id, item::class.java.simpleName)
        }

        binding.fab.setOnClickListener {
            (activity as? MainActivity)?.showCreateItem()
        }

        binding.fab.show()
    }
    private fun setupModeSwitcher() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Библиотека"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Google Books"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    MODE_LIBRARY -> {
                        currentMode = MODE_LIBRARY
                        loadLibraryItems(repository, initialLoad = true)
                        binding.searchContainer.visibility = View.GONE
                        binding.sortButtonsLayout.visibility = View.VISIBLE
                    }

                    MODE_GOOGLE_BOOKS -> {
                        currentMode = MODE_GOOGLE_BOOKS
                        adapter.updateItems(emptyList())
                        binding.fab.visibility = View.GONE
                        binding.searchContainer.visibility = View.VISIBLE
                        binding.sortButtonsLayout.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) { }

            override fun onTabReselected(tab: TabLayout.Tab?) { }

        })

        binding.btnSearch.setOnClickListener {
            searchGoogleBooks()
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


    fun scrollToLastItem() {
        val lastPosition = adapter.itemCount - 1
        if (lastPosition >= 0) {
            binding.recyclerView.layoutManager?.scrollToPosition(lastPosition)
        }
    }

    private fun searchGoogleBooks() {
        googleBooksSearchJob?.cancel()
        googleBooksSearchJob = lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE

                val author = binding.etAuthor.text.toString().takeIf { it.isNotBlank() }
                val title = binding.etTitle.text.toString().takeIf { it.isNotBlank() }

                val books = googleBooksRepository.searchBooks(author, title)

                if (books.isEmpty()) {
                    binding.errorTextView.text = "Книги не найдены"
                    binding.errorTextView.visibility = View.VISIBLE
                } else {
                    adapter.updateItems(books)
                    binding.recyclerView.visibility = View.VISIBLE
                }
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView(repository: LibraryRepository) {
        adapter = LibraryAdapter(emptyList()).apply {
            onItemClick = { item ->
                if (currentMode == MODE_LIBRARY) {
                    (activity as? MainActivity)?.showItemDetails(item.id, item::class.java.simpleName)
                }
            }

            onItemLongClick = { item ->
                if (currentMode == MODE_GOOGLE_BOOKS && item is Book) {
                    saveBookToLibrary(item, repository)
                }
            }
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!isLoading && dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                    if ((firstVisibleItem + visibleItemCount) >= totalItemCount - VISIBLE_ITEMS) {
                        loadLibraryItems(repository, loadMore = true)
                    }
                }
            }
        })
    }

    private fun setupSortingButtons(repository: LibraryRepository) {
        binding.sortByName.setOnClickListener {
            lifecycleScope.launch {
                repository.setSortOrder(LibraryRepository.SortOrder.BY_NAME)
                loadLibraryItems(repository)
            }
        }

        binding.sortByDate.setOnClickListener {
            lifecycleScope.launch {
                repository.setSortOrder(LibraryRepository.SortOrder.BY_DATE)
                loadLibraryItems(repository)
            }
        }
    }

    private fun saveBookToLibrary(book: Book, repository: LibraryRepository) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.addItem(book)
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

    private fun loadLibraryItems(
        repository: LibraryRepository,
        initialLoad: Boolean = false,
        loadMore: Boolean = false,
    ) {
        if (isLoading) return

        isLoading = true
        if (!loadMore) {
            showShimmer()
        } else {
            binding.progressBar.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getLibraryItems(loadMore)
                }

                when {
                    result.isSuccess -> {
                        val items = result.getOrNull() ?: emptyList()
                        if (items.isEmpty() && !loadMore && !initialLoad) {
                            binding.errorTextView.text = "Нет элементов для отображения"
                            binding.errorTextView.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        } else {
                            adapter.updateItems(items)
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.errorTextView.visibility = View.GONE

                            if (initialLoad) {
                                delay(1000)
                            }
                        }
                    }

                    result.isFailure -> {
                        binding.errorTextView.text =
                            result.exceptionOrNull()?.message ?: "Ошибка загрузки"
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                }
            } finally {
                hideShimmer()
                binding.progressBar.visibility = View.GONE
                isLoading = false
                isFirstLoad = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        job?.cancel()
    }

    private fun showShimmer() {
        binding.shimmerViewContainer.bringToFront()
        binding.shimmerViewContainer.startShimmer()
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
    }
}