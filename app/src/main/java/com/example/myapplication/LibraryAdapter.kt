package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemLibraryBinding

class LibraryAdapter(private val items: List<LibraryItem>) :
    RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    var onItemClick: ((LibraryItem) -> Unit)? = null

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
                    println("DEBUG: Item clicked! ID: ${item.id}")
                    onItemClick?.invoke(item)
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