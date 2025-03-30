package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val library = Library()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = LibraryAdapter(library.libraryItems)
        recyclerView.adapter = adapter

        adapter.onItemClick = { item ->

            Toast.makeText(
                this,
                "Элемент с id ${item.id}",
                Toast.LENGTH_SHORT
            ).show()

            item.isAvailable = !item.isAvailable
            adapter.notifyItemChanged(library.libraryItems.indexOf(item))
        }
    }
}