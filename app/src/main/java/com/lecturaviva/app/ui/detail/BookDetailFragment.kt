package com.lecturaviva.app.ui.detail

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lecturaviva.app.R
import com.lecturaviva.app.databinding.FragmentBookDetailBinding
import com.lecturaviva.app.databinding.ItemNoteBinding
import com.lecturaviva.app.domain.model.BookStatus
import com.lecturaviva.app.domain.model.Note
import com.lecturaviva.app.ui.common.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookDetailFragment : Fragment() {

    private var _b: FragmentBookDetailBinding? = null
    private val b get() = _b!!
    private val vm: BookDetailViewModel by viewModels()
    private val args: BookDetailFragmentArgs by navArgs()
    private lateinit var noteAdapter: NoteAdapter
    private var spinnerReady = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentBookDetailBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadBook(args.bookId)

        noteAdapter = NoteAdapter { note -> vm.deleteNote(note) }
        b.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        b.rvNotes.adapter = noteAdapter

        val statuses = BookStatus.entries.map { it.displayName }
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_status, statuses)
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_status_dropdown)
        b.spinnerStatus.adapter = spinnerAdapter

        vm.book.observe(viewLifecycleOwner) { book ->
            book ?: return@observe
            b.tvTitle.text         = book.title
            b.tvAuthor.text        = "${book.author} · ${book.year}"
            b.tvProgress.text      = "${book.progressPercent}%"
            b.tvPages.text         = "${book.currentPage} / ${book.totalPages} páginas"
            b.progressBar.progress = book.progressPercent
            b.etReview.setText(book.review)
            b.ratingBar.rating     = book.rating

            if (book.isFavorite) {
                b.btnFavorite.setImageResource(R.drawable.ic_favorite)
                b.btnFavorite.setColorFilter(android.graphics.Color.parseColor("#E53935"))
            } else {
                b.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
                b.btnFavorite.clearColorFilter()
            }

            if (book.coverUrl.isNotEmpty()) {
                Glide.with(this).load(book.coverUrl).placeholder(R.drawable.ic_book).into(b.ivCover)
            }

            spinnerReady = false
            val idx = BookStatus.entries.indexOfFirst { it == book.status }
            if (idx >= 0) b.spinnerStatus.setSelection(idx, false)
            b.spinnerStatus.post { spinnerReady = true }
        }

        vm.notes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
            b.tvEmptyNotes.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }

        b.btnUpdateProgress.setOnClickListener {
            val book = vm.book.value ?: return@setOnClickListener
            showProgressDialog(book.currentPage, book.totalPages) { vm.updateProgress(it) }
        }

        b.btnFavorite.setOnClickListener { vm.toggleFavorite() }

        b.btnAddNote.setOnClickListener {
            showAddNoteDialog { text, chapter, page -> vm.addNote(text, chapter, page) }
        }

        b.btnSaveReview.setOnClickListener { vm.saveReview(b.etReview.text.toString()) }

        b.btnSaveRating.setOnClickListener {
            val stars = b.ratingBar.rating
            if (stars > 0f) vm.saveRating(stars)
            else showErrorDialog("Valoración", "Selecciona al menos media estrella.")
        }

        b.btnDelete.setOnClickListener {
            showConfirmDialog(
                title        = "Eliminar libro",
                message      = "¿Seguro que quieres eliminar este libro? Esta acción no se puede deshacer.",
                positiveText = "Sí, eliminar"
            ) { vm.deleteBook() }
        }

        b.btnBack.setOnClickListener { findNavController().popBackStack() }

        b.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (!spinnerReady) return
                val selected = BookStatus.entries[pos]
                val current  = vm.book.value?.status ?: return
                if (selected != current) vm.updateStatus(selected)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        lifecycleScope.launch {
            vm.uiEvent.collectLatest { event ->
                event ?: return@collectLatest
                when (event) {
                    is DetailEvent.ProgressUpdated ->
                        showSuccessDialog("Progreso actualizado", "Tu progreso de lectura se ha guardado.")
                    is DetailEvent.ReviewSaved ->
                        showSuccessDialog("Reseña guardada", "Reseña guardada correctamente.")
                    is DetailEvent.NoteAdded ->
                        showSuccessDialog("Nota añadida", "La nota se ha guardado correctamente.")
                    is DetailEvent.RatingSaved ->
                        showSuccessDialog("Valoración guardada", "Has valorado este libro con ${event.stars} estrellas.")
                    is DetailEvent.StatusUpdated ->
                        showInfoDialog("Estado actualizado", "El libro ahora está como: ${event.s.displayName}.")
                    is DetailEvent.BookFinished ->
                        showSuccessDialog("¡Libro terminado!", "¿Quieres valorarlo? Desplázate abajo para valorar.")
                    is DetailEvent.BookDeleted ->
                        findNavController().popBackStack()
                    is DetailEvent.Error ->
                        showErrorDialog("Error", event.msg)
                }
                vm.clearEvent()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

class NoteAdapter(private val onDelete: (Note) -> Unit) :
    ListAdapter<Note, NoteAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    inner class VH(private val b: ItemNoteBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(note: Note) {
            b.tvNoteText.text = note.text
            b.btnDeleteNote.setOnClickListener { onDelete(note) }

            val location = when {
                note.chapter > 0 && note.page > 0 -> "Cap. ${note.chapter} · Pág. ${note.page}"
                note.chapter > 0 -> "Cap. ${note.chapter}"
                note.page > 0    -> "Pág. ${note.page}"
                else             -> null
            }
            if (location != null) {
                b.tvNoteLocation.text = location
                b.tvNoteLocation.visibility = View.VISIBLE
            } else {
                b.tvNoteLocation.visibility = View.GONE
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(a: Note, b: Note)    = a.id == b.id
            override fun areContentsTheSame(a: Note, b: Note) = a == b
        }
    }
}