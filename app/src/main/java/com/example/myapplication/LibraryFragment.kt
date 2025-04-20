package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LibraryAdapter
    private val library = Library.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        setupRecyclerView()

        adapter.onItemClick = { item ->
            (activity as? MainActivity)?.showItemDetails(item.id, item::class.java.simpleName)
        }

        binding.fab.setOnClickListener {
            (activity as? MainActivity)?.showCreateItem()
        }

        binding.fab.show()
    }

    fun scrollToLastItem() {
        val lastPosition = library.libraryItems.size - 1
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
    }
}