package com.lecturaviva.app.ui.auth

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.lecturaviva.app.databinding.FragmentRegisterBinding
import com.lecturaviva.app.databinding.FragmentRecoverBinding
import com.lecturaviva.app.ui.common.showErrorDialog
import com.lecturaviva.app.ui.common.showSuccessDialog
import dagger.hilt.android.AndroidEntryPoint

// ── RegisterFragment ──────────────────────────────────────────────────────────
@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _b: FragmentRegisterBinding? = null
    private val b get() = _b!!
    private val vm: AuthViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentRegisterBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.btnRegister.setOnClickListener {
            vm.register(
                b.etName.text.toString().trim(),
                b.etEmail.text.toString().trim(),
                b.etPassword.text.toString()
            )
        }

        b.tvGoLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        vm.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading    -> b.btnRegister.isEnabled = false
                is AuthState.Registered -> {
                    b.btnRegister.isEnabled = true
                    showSuccessDialog("¡Bienvenid@!", "Tu cuenta se ha creado correctamente.") {
                        (activity as? AuthActivity)?.goToMain()
                    }
                }
                is AuthState.Error -> {
                    b.btnRegister.isEnabled = true
                    showErrorDialog("Error", state.message)
                }
                else -> b.btnRegister.isEnabled = true
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── RecoverFragment ───────────────────────────────────────────────────────────
@AndroidEntryPoint
class RecoverFragment : Fragment() {

    private var _b: FragmentRecoverBinding? = null
    private val b get() = _b!!
    private val vm: AuthViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentRecoverBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.btnSend.setOnClickListener {
            vm.sendPasswordReset(b.etEmail.text.toString().trim())
        }

        b.tvGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        vm.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading   -> b.btnSend.isEnabled = false
                is AuthState.ResetSent -> {
                    b.btnSend.isEnabled = true
                    showSuccessDialog(
                        "Correo enviado",
                        "Revisa tu bandeja de entrada y sigue las instrucciones."
                    )
                }
                is AuthState.Error -> {
                    b.btnSend.isEnabled = true
                    showErrorDialog("Error", state.message)
                }
                else -> b.btnSend.isEnabled = true
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}