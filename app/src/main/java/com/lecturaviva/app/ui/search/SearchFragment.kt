package com.lecturaviva.app.ui.search

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lecturaviva.app.R
import com.lecturaviva.app.databinding.FragmentSearchBinding
import com.lecturaviva.app.databinding.ItemSearchResultBinding
import com.lecturaviva.app.domain.model.Book
import com.lecturaviva.app.ui.common.showErrorDialog
import com.lecturaviva.app.ui.common.showInfoDialog
import com.lecturaviva.app.ui.common.showSuccessDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _b: FragmentSearchBinding? = null
    private val b get() = _b!!
    private val vm: SearchViewModel by viewModels()

    private lateinit var adapter: SearchResultAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSearchBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SearchResultAdapter(
            onAddClick   = { book -> vm.addBook(book) },
            onBookClick  = { book -> vm.addBook(book) },
            libraryKeys  = emptySet()
        )
        b.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.recyclerView.adapter = adapter

        // Búsqueda por texto
        b.btnSearch.setOnClickListener { doSearch() }
        b.etSearch.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH) { doSearch(); true } else false
        }

        // Chips de categoría
        val catChips = mapOf(
            b.chipAll       to null,
            b.chipFantasy   to "fantasy",
            b.chipScifi     to "science fiction",
            b.chipThriller  to "thriller",
            b.chipClassic   to "classic",
            b.chipNonfiction to "nonfiction",
            b.chipRomance   to "romance",
            b.chipHorror    to "horror"
        )
        catChips.forEach { (chip, cat) ->
            chip.setOnClickListener {
                val q = if (cat != null) cat else b.etSearch.text.toString().trim()
                if (q.isNotBlank()) vm.search(q)
            }
        }

        vm.results.observe(viewLifecycleOwner) { books ->
            vm.libraryKeys.value?.let { keys ->
                adapter.libraryKeys = keys
            }
            adapter.submitList(books)
            b.tvEmpty.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.libraryKeys.observe(viewLifecycleOwner) { keys ->
            adapter.libraryKeys = keys
            adapter.notifyDataSetChanged()
        }

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            b.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        lifecycleScope.launch {
            vm.event.collectLatest { event ->
                event ?: return@collectLatest
                when (event) {
                    is SearchEvent.BookAdded -> showSuccessDialog("Libro añadido", "«${event.title}» se ha añadido a tu biblioteca como Pendiente.")
                    is SearchEvent.Error     -> showErrorDialog("Error", event.msg)
                }
                vm.clearEvent()
            }
        }
    }

    private fun doSearch() {
        val q = b.etSearch.text.toString().trim()
        if (q.isNotBlank()) vm.search(q)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

class SearchResultAdapter(
    private val onAddClick: (Book) -> Unit,
    private val onBookClick: (Book) -> Unit,
    var libraryKeys: Set<String>
) : ListAdapter<Book, SearchResultAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    inner class VH(private val b: ItemSearchResultBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(book: Book) {
            b.tvTitle.text  = book.title
            b.tvAuthor.text = book.author
            b.tvYear.text   = if (book.year > 0) book.year.toString() else ""
            b.tvPages.text  = if (book.totalPages > 0) "${book.totalPages} pág." else ""

            val inLibrary = libraryKeys.contains(book.openLibraryKey) && book.openLibraryKey.isNotEmpty()
            if (inLibrary) {
                b.btnAdd.visibility       = View.GONE
                b.tvInLibrary.visibility  = View.VISIBLE
            } else {
                b.btnAdd.visibility       = View.VISIBLE
                b.tvInLibrary.visibility  = View.GONE
                b.btnAdd.setOnClickListener { onAddClick(book) }
            }

            if (book.coverUrl.isNotEmpty()) {
                Glide.with(b.ivCover).load(book.coverUrl).placeholder(R.drawable.logo_lecturaviva).into(b.ivCover)
            } else {
                b.ivCover.setImageResource(R.drawable.logo_lecturaviva)
            }
            b.root.setOnClickListener { onBookClick(book) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(a: Book, b: Book)    = a.openLibraryKey == b.openLibraryKey
            override fun areContentsTheSame(a: Book, b: Book) = a == b
        }
    }
}
