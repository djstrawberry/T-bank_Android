package com.example.myapplication

import android.content.Context
import android.content.res.Configuration
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("FRAGMENT_DEBUG: onCreate() called")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        println("FRAGMENT_DEBUG: onAttach() called")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        setupRecyclerView()

        adapter.onItemClick = { item ->
            (activity as? MainActivity)?.showItemFragment(item.id, item::class.java.simpleName)
        }

        binding.fab.setOnClickListener {
            (activity as? MainActivity)?.showCreateFragment()
        }

        binding.fab.show()
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
