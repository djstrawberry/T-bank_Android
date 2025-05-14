package com.example.myapplication.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.domain.models.LibraryItem
import com.example.myapplication.domain.repositories.LibraryRepository
import com.example.myapplication.R
import com.example.myapplication.common.LibraryAdapter
import com.example.myapplication.databinding.FragmentLibraryBinding
import com.example.myapplication.presentation.MyApplication
import com.example.myapplication.presentation.activities.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment(R.layout.fragment_library) {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LibraryAdapter
    private lateinit var repository: LibraryRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)
        repository = (requireActivity().application as MyApplication).libraryRepository

        setupRecyclerView()
        loadItems()
    }

    private fun setupRecyclerView() {
        adapter = LibraryAdapter(emptyList()).apply {
            onItemClick = { item ->
                (activity as? MainActivity)?.showItemDetails(item.id, item::class.simpleName ?: "")
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LibraryFragment.adapter
        }
    }

    private fun loadItems() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getLibraryItems(false)
            }
            result.onSuccess { items ->
                adapter.updateItems(items)
            }.onFailure {
               Toast.makeText(requireContext(), "Ой, что-то пошло не так :(", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}