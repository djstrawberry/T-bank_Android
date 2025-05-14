package com.example.myapplication.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemLibraryBinding
import com.example.myapplication.domain.models.Book
import com.example.myapplication.domain.models.Disk
import com.example.myapplication.domain.models.LibraryItem
import com.example.myapplication.domain.models.Newspaper

class LibraryAdapter(private var items: List<LibraryItem>) :
    RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    fun updateItems(newItems: List<LibraryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    var onItemClick: ((LibraryItem) -> Unit)? = null
    var onItemLongClick: ((LibraryItem) -> Unit)? = null

    inner class LibraryViewHolder(private val binding: ItemLibraryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LibraryItem) {
            with(binding) {
                itemName.text = item.name
                itemId.text = "ID: ${item.id}"

                val icon = when (item) {
                    is Book -> R.drawable.ic_book
                    is Newspaper -> R.drawable.ic_newspaper
                    is Disk -> R.drawable.ic_disk
                    else -> R.drawable.ic_default
                }
                itemIcon.setImageResource(icon)

                cardView.cardElevation = if (item.isAvailable) 10f else 1f

                itemView.setOnClickListener {
                    onItemClick?.invoke(item)
                }

                itemView.setOnLongClickListener {
                    onItemLongClick?.invoke(item)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val binding = ItemLibraryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LibraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}