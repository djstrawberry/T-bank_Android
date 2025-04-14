package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LibraryAdapter
    private val library = Library.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = LibraryAdapter(library.libraryItems)
        binding.recyclerView.adapter = adapter

        adapter.onItemClick = { item ->
            val intent = Intent(this, ItemActivity::class.java).apply {
                putExtra("ITEM_ID", item.id)
                putExtra("ITEM_TYPE", item.javaClass.simpleName)
            }
            startActivity(intent)
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this, ItemActivity::class.java).apply {
                putExtra("IS_EDIT_MODE", true)
            }
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}