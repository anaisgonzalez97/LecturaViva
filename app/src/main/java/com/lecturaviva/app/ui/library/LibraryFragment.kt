package com.lecturaviva.app.ui.library

import android.os.Bundle
import android.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lecturaviva.app.databinding.FragmentLibraryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _b: FragmentLibraryBinding? = null
    private val b get() = _b!!
    private val vm: LibraryViewModel by viewModels()
    private lateinit var adapter: BookAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentLibraryBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BookAdapter(
            onBookClick = { book ->
                val action = LibraryFragmentDirections.actionLibraryToDetail(book.id)
                findNavController().navigate(action)
            },
            onFavoriteClick = { book -> vm.toggleFavorite(book) }
        )
        b.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerView.adapter = adapter

        // Filtro desde argumentos (viniendo de Home al pulsar estadística)
        arguments?.getString("filter")?.let { filterName ->
            val f = LibraryFilter.entries.find { it.name == filterName } ?: LibraryFilter.All
            vm.setFilter(f)
            selectChip(f)
        }

        // Chips
        b.chipAll.setOnClickListener       { vm.setFilter(LibraryFilter.All);       selectChip(LibraryFilter.All) }
        b.chipReading.setOnClickListener   { vm.setFilter(LibraryFilter.Reading);   selectChip(LibraryFilter.Reading) }
        b.chipPending.setOnClickListener   { vm.setFilter(LibraryFilter.Pending);   selectChip(LibraryFilter.Pending) }
        b.chipRead.setOnClickListener      { vm.setFilter(LibraryFilter.Read);      selectChip(LibraryFilter.Read) }
        b.chipFavorites.setOnClickListener { vm.setFilter(LibraryFilter.Favorites); selectChip(LibraryFilter.Favorites) }
        b.chipAbandoned.setOnClickListener { vm.setFilter(LibraryFilter.Abandoned); selectChip(LibraryFilter.Abandoned) }

        // Buscador
        b.etSearch.doAfterTextChanged { vm.setQuery(it?.toString() ?: "") }

        // Lista
        vm.books.observe(viewLifecycleOwner) { books ->
            adapter.submitList(books)
            b.tvEmpty.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun selectChip(f: LibraryFilter) {
        b.chipAll.isChecked       = f == LibraryFilter.All
        b.chipReading.isChecked   = f == LibraryFilter.Reading
        b.chipPending.isChecked   = f == LibraryFilter.Pending
        b.chipRead.isChecked      = f == LibraryFilter.Read
        b.chipFavorites.isChecked = f == LibraryFilter.Favorites
        b.chipAbandoned.isChecked = f == LibraryFilter.Abandoned
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}