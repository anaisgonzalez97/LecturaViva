package com.lecturaviva.app.ui.home

import android.os.Bundle
import androidx.core.os.bundleOf
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.lecturaviva.app.R
import com.lecturaviva.app.databinding.FragmentHomeBinding
import com.lecturaviva.app.domain.model.BookStatus
import com.lecturaviva.app.ui.common.showErrorDialog
import com.lecturaviva.app.ui.common.showProgressDialog
import com.lecturaviva.app.ui.common.showSuccessDialog
import com.lecturaviva.app.ui.library.LibraryFilter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.tvWelcome.text = "¡Hola, ${vm.userName}!"

        // Libro en curso
        vm.currentlyReading.observe(viewLifecycleOwner) { book ->
            if (book == null) {
                b.cardReading.visibility = View.GONE
                b.tvNoBook.visibility    = View.VISIBLE
            } else {
                b.cardReading.visibility = View.VISIBLE
                b.tvNoBook.visibility    = View.GONE
                b.tvBookTitle.text  = book.title
                b.tvBookAuthor.text = book.author
                b.tvProgress.text   = "${book.progressPercent}%"
                b.tvPages.text      = "Página ${book.currentPage} de ${book.totalPages}"
                b.progressBar.progress = book.progressPercent
                if (book.coverUrl.isNotEmpty()) {
                    Glide.with(this).load(book.coverUrl).into(b.ivCover)
                }
                b.btnUpdateProgress.setOnClickListener {
                    showProgressDialog(book.currentPage, book.totalPages) { vm.updateProgress(book, it) }
                }
                b.cardReading.setOnClickListener {
                    val action = HomeFragmentDirections.actionHomeToDetail(book.id)
                    findNavController().navigate(action)
                }
            }
        }

        // Estadísticas
        vm.countRead.observe(viewLifecycleOwner)      { b.tvCountRead.text      = it.toString() }
        vm.countReading.observe(viewLifecycleOwner)   { b.tvCountReading.text   = it.toString() }
        vm.countPending.observe(viewLifecycleOwner)   { b.tvCountPending.text   = it.toString() }
        vm.countFavorites.observe(viewLifecycleOwner) { b.tvCountFavorites.text = it.toString() }

        // Navegar a biblioteca filtrada al tocar estadística
        b.cardRead.setOnClickListener {
            findNavController().navigate(R.id.libraryFragment,
                androidx.core.os.bundleOf("filter" to LibraryFilter.Read.name))
        }
        b.cardReading2.setOnClickListener {
            findNavController().navigate(R.id.libraryFragment,
                androidx.core.os.bundleOf("filter" to LibraryFilter.Reading.name))
        }
        b.cardPending.setOnClickListener {
            findNavController().navigate(R.id.libraryFragment,
                androidx.core.os.bundleOf("filter" to LibraryFilter.Pending.name))
        }
        b.cardFavorites.setOnClickListener {
            findNavController().navigate(R.id.libraryFragment,
                androidx.core.os.bundleOf("filter" to LibraryFilter.Favorites.name))
        }

        // Resultado de actualizar progreso
        lifecycleScope.launch {
            vm.updateResult.collectLatest { result ->
                result ?: return@collectLatest
                when {
                    result.isSuccess -> showSuccessDialog("Progreso actualizado", "Tu progreso de lectura se ha guardado correctamente.")
                    result.isError   -> showErrorDialog("Error", (result as com.lecturaviva.app.util.Result.Error).message)
                }
                vm.clearUpdateResult()
            }
        }

        vm.syncFromCloud()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
