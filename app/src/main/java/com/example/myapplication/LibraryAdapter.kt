package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ImageView
import androidx.cardview.widget.CardView

class LibraryAdapter(val items: List<LibraryItem>) :
    RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    var onItemClick: ((LibraryItem) -> Unit)? = null

    inner class LibraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.itemName)
        private val idTextView: TextView = itemView.findViewById(R.id.itemId)
        private val iconImageView: ImageView = itemView.findViewById(R.id.itemIcon)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)

        fun bind(item: LibraryItem) {
            nameTextView.text = item.name
            idTextView.text = "ID: ${item.id}"

            val icon = when (item) {
                is Book -> R.drawable.ic_book
                is Newspaper -> R.drawable.ic_newspaper
                is Disk -> R.drawable.ic_disk
                else -> R.drawable.ic_default
            }
            iconImageView.setImageResource(icon)

            cardView.cardElevation = if (item.isAvailable) 10f else 1f

            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_library, parent, false)
        return LibraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}