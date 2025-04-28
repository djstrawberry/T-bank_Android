package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentLibraryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LibraryAdapter
    private val library = Library.getInstance()
    private var job: Job? = null
    private var isLoading = false
    private var isFirstLoad = true
    private var totalItems = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        val db = GetDb.getDatabase(requireContext())

        val repository = LibraryRepository(db)

        setupRecyclerView(repository)
        setupSortingButtons(repository)
        loadLibraryItems(repository, initialLoad = true)

        adapter.onItemClick = { item ->
            (activity as? MainActivity)?.showItemDetails(item.id, item::class.java.simpleName)
        }

        binding.fab.setOnClickListener {
            (activity as? MainActivity)?.showCreateItem()
        }

        binding.fab.show()
    }

    fun scrollToLastItem() {
        val lastPosition = adapter.itemCount - 1
        if (lastPosition >= 0) {
            binding.recyclerView.layoutManager?.scrollToPosition(lastPosition)
        }
    }

    private fun setupRecyclerView(repository: LibraryRepository) {
        adapter = LibraryAdapter(emptyList())
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

                    if ((firstVisibleItem + visibleItemCount) >= totalItemCount - 5) {
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

    private fun loadLibraryItems(
        repository: LibraryRepository,
        initialLoad: Boolean = false,
        loadMore: Boolean = false,
        isScrollingUp: Boolean = false
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
                        binding.errorTextView.text = result.exceptionOrNull()?.message ?: "Ошибка загрузки"
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