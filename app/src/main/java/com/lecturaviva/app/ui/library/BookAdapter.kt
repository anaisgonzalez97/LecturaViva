package com.lecturaviva.app.ui.library

import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lecturaviva.app.R
import com.lecturaviva.app.databinding.ItemBookBinding
import com.lecturaviva.app.domain.model.Book

class BookAdapter(
    private val onBookClick: (Book) -> Unit,
    private val onFavoriteClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemBookBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(book: Book) {
            b.tvTitle.text  = book.title
            b.tvAuthor.text = book.author
            b.tvStatus.text = book.status.displayName
            b.progressBar.progress = book.progressPercent
            b.tvProgress.text      = "${book.progressPercent}%"
            b.ivFavorite.setImageResource(
                if (book.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            if (book.coverUrl.isNotEmpty()) {
                Glide.with(b.ivCover).load(book.coverUrl).placeholder(R.drawable.logo_lecturaviva).into(b.ivCover)
            } else {
                b.ivCover.setImageResource(R.drawable.logo_lecturaviva)
            }
            b.root.setOnClickListener       { onBookClick(book) }
            b.ivFavorite.setOnClickListener { onFavoriteClick(book) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(a: Book, b: Book)    = a.id == b.id
            override fun areContentsTheSame(a: Book, b: Book) = a == b
        }
    }
}
