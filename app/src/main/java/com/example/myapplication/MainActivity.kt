package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding.recyclerView) {
            val library = Library()
            val adapter = LibraryAdapter(library.libraryItems)
            this.adapter = adapter

            adapter.onItemClick = { item ->

                Toast.makeText(
                    this@MainActivity,
                    "Элемент с id ${item.id}",
                    Toast.LENGTH_SHORT
                ).show()

                item.isAvailable = !item.isAvailable
                adapter.notifyItemChanged(library.libraryItems.indexOf(item))
            }
        }
    }
}