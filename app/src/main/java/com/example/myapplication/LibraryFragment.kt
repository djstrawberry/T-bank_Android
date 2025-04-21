package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentLibraryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LibraryAdapter
    private val library = Library.getInstance()
    private val repository = LibraryRepository()
    private var job: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        setupRecyclerView()
        loadLibraryItems()

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

    private fun setupRecyclerView() {
        adapter = LibraryAdapter(library.libraryItems)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        job?.cancel()
    }

    private fun showShimmer() {
        binding.shimmerViewContainer.startShimmer()
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
    }

    private fun loadLibraryItems() {
        showShimmer()

        job = lifecycleScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                repository.getLibraryItems()
            }

            hideShimmer()

                when {
                    result.isSuccess ->  {
                        val items = result.getOrNull() ?: emptyList()
                        val currentOnItemClick = adapter.onItemClick
                        adapter = LibraryAdapter(items)
                        adapter.onItemClick = currentOnItemClick
                        binding.recyclerView.adapter = adapter
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                    result.isFailure -> {
                        binding.errorTextView.text = result.exceptionOrNull()?.message ?: "Данные не были загружены."
                        binding.errorTextView.visibility = View.VISIBLE
                }
            }
        }
    }
}