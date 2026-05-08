package com.lecturaviva.app.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.lecturaviva.app.R
import com.lecturaviva.app.databinding.FragmentProfileBinding
import com.lecturaviva.app.ui.auth.AuthActivity
import com.lecturaviva.app.ui.common.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!
    private val vm: ProfileViewModel by viewModels()
    private var selectedPhotoUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        val file = java.io.File(requireContext().filesDir, "profile_photo_${vm.userId}.jpg")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        selectedPhotoUri = Uri.fromFile(file)
        vm.savePhotoUri(file.absolutePath)
        Glide.with(this).load(file).circleCrop().into(b.ivAvatar)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProfileBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshUi()

        b.ivAvatar.setOnClickListener { pickImage.launch("image/*") }
        b.btnEditProfile.setOnClickListener { showEditProfileDialog() }
        b.btnSync.setOnClickListener { vm.syncFromCloud() }
        b.btnReport.setOnClickListener { showReportDialog() }

        b.btnLogout.setOnClickListener {
            showConfirmDialog(
                title        = "Cerrar sesión",
                message      = "¿Seguro que quieres cerrar sesión?",
                positiveText = "Sí, cerrar sesión"
            ) { vm.logout() }
        }

        lifecycleScope.launch {
            vm.event.collectLatest { event ->
                event ?: return@collectLatest
                when (event) {
                    is ProfileEvent.ProfileUpdated -> {
                        showSuccessDialog("Perfil actualizado", "Tus datos se han guardado correctamente.")
                        refreshUi()
                    }
                    is ProfileEvent.LoggedOut -> {
                        startActivity(Intent(requireContext(), AuthActivity::class.java))
                        requireActivity().finish()
                    }
                    is ProfileEvent.Synced     -> showSuccessDialog("Sincronizado", "Datos sincronizados correctamente.")
                    is ProfileEvent.ReportSent -> showSuccessDialog("Reporte enviado", "Gracias por ayudarnos a mejorar la app.")
                    is ProfileEvent.Error      -> showErrorDialog("Error", event.msg)
                }
                vm.clearEvent()
            }
        }
    }

    private fun refreshUi() {
        val user = vm.currentUser
        b.tvName.text  = user?.name  ?: ""
        b.tvEmail.text = user?.email ?: ""
        val savedPhoto = vm.getSavedPhotoUri()
        if (savedPhoto.isNotEmpty()) {
            val file = java.io.File(savedPhoto)
            if (file.exists())
                Glide.with(this).load(file).circleCrop().into(b.ivAvatar)
            else
                b.ivAvatar.setImageResource(R.drawable.ic_person)
        } else {
            b.ivAvatar.setImageResource(R.drawable.ic_person)
        }
    }

    private fun showReportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_report, null)
        val etType    = dialogView.findViewById<TextInputEditText>(R.id.etReportType)
        val etMessage = dialogView.findViewById<TextInputEditText>(R.id.etReportMessage)

        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_LecturaViva_Dialog)
            .setTitle("Reportar un problema")
            .setView(dialogView)
            .setPositiveButton("Enviar") { _, _ ->
                val type    = etType.text.toString().trim()
                val message = etMessage.text.toString().trim()
                if (message.isNotEmpty()) {
                    vm.sendReport(type, message)
                } else {
                    showErrorDialog("Error", "Por favor describe el problema antes de enviar.")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditProfileDialog() {
        val user = vm.currentUser ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile_full, null)
        val etName     = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etEmail    = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        etName.setText(user.name)
        etEmail.setText(user.email)

        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_LecturaViva_Dialog)
            .setTitle("Editar perfil")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name     = etName.text.toString().trim()
                val email    = etEmail.text.toString().trim()
                val password = etPassword.text.toString()
                val photoUri = selectedPhotoUri?.toString() ?: vm.getSavedPhotoUri()
                vm.updateProfile(name, email, password, photoUri)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}