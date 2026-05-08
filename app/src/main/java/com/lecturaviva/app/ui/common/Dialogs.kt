package com.lecturaviva.app.ui.common

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.lecturaviva.app.R

fun Fragment.showProgressDialog(
    currentPage: Int,
    totalPages: Int,
    onSave: (Int) -> Unit
) {
    val context = requireContext()
    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
    val tvTotal    = dialogView.findViewById<TextView>(R.id.tvTotalPages)
    val rgMode     = dialogView.findViewById<RadioGroup>(R.id.rgMode)
    val rbPage     = dialogView.findViewById<RadioButton>(R.id.rbPage)
    val rbPercent  = dialogView.findViewById<RadioButton>(R.id.rbPercent)
    val tilPage    = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPage)
    val tilPercent = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPercent)
    val etPage     = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPage)
    val etPercent  = dialogView.findViewById<TextInputEditText>(R.id.etPercent)

    tvTotal.text = "Total: $totalPages páginas"
    etPage.setText(if (currentPage > 0) currentPage.toString() else "")

    rgMode.setOnCheckedChangeListener { _, checkedId ->
        if (checkedId == R.id.rbPage) {
            tilPage.visibility    = android.view.View.VISIBLE
            tilPercent.visibility = android.view.View.GONE
        } else {
            tilPage.visibility    = android.view.View.GONE
            tilPercent.visibility = android.view.View.VISIBLE
        }
    }

    MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_LecturaViva_Dialog)
        .setTitle("Actualizar progreso")
        .setView(dialogView)
        .setPositiveButton("Guardar") { _, _ ->
            val page = if (rbPage.isChecked) {
                etPage.text.toString().toIntOrNull() ?: 0
            } else {
                val pct = etPercent.text.toString().toIntOrNull() ?: 0
                if (totalPages > 0) (pct * totalPages) / 100 else 0
            }
            onSave(page)
        }
        .setNegativeButton("Cancelar", null)
        .show()
}

fun Fragment.showSuccessDialog(
    title: String, message: String, onDismiss: (() -> Unit)? = null
) {
    MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_LecturaViva_Dialog)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Aceptar") { _, _ -> onDismiss?.invoke() }
        .show()
}

fun Fragment.showErrorDialog(title: String, message: String) {
    MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_LecturaViva_Dialog)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Aceptar", null)
        .show()
}

fun Fragment.showInfoDialog(
    title: String, message: String, onDismiss: (() -> Unit)? = null
) {
    MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_LecturaViva_Dialog)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Aceptar") { _, _ -> onDismiss?.invoke() }
        .show()
}

fun Fragment.showConfirmDialog(
    title: String, message: String, positiveText: String = "Confirmar",
    onConfirm: () -> Unit
) {
    MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_LecturaViva_Dialog)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText) { _, _ -> onConfirm() }
        .setNegativeButton("Cancelar", null)
        .show()
}

// Ahora devuelve texto, capítulo y página
fun Fragment.showAddNoteDialog(onSave: (text: String, chapter: Int, page: Int) -> Unit) {
    val context = requireContext()
    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_note, null)
    val etNote    = dialogView.findViewById<TextInputEditText>(R.id.etNote)
    val etChapter = dialogView.findViewById<TextInputEditText>(R.id.etChapter)
    val etPage    = dialogView.findViewById<TextInputEditText>(R.id.etPage)

    MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_LecturaViva_Dialog)
        .setTitle("Añadir nota")
        .setView(dialogView)
        .setPositiveButton("Guardar") { _, _ ->
            val text    = etNote.text.toString().trim()
            val chapter = etChapter.text.toString().toIntOrNull() ?: 0
            val page    = etPage.text.toString().toIntOrNull() ?: 0
            if (text.isNotEmpty()) onSave(text, chapter, page)
        }
        .setNegativeButton("Cancelar", null)
        .show()
}