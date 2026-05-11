package com.lecturaviva.app.ui.auth

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.lecturaviva.app.R
import com.lecturaviva.app.databinding.FragmentLoginBinding
import com.lecturaviva.app.ui.common.showErrorDialog
import com.lecturaviva.app.ui.common.showSuccessDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!
    private val vm: AuthViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentLoginBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.btnLogin.setOnClickListener {
            vm.login(
                b.etEmail.text.toString().trim(),
                b.etPassword.text.toString()
            )
        }

        b.btnCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        b.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_recover)
        }

        vm.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> b.btnLogin.isEnabled = false
                is AuthState.Success -> {
                    b.btnLogin.isEnabled = true
                    val nombre = vm.currentUserName()
                    showSuccessDialog(
                        "¡Bienvenido/a, $nombre!",
                        "Has iniciado sesión correctamente."
                    ) {
                        (activity as? AuthActivity)?.goToMain()
                    }
                }

                is AuthState.Error -> {
                    b.btnLogin.isEnabled = true
                    showErrorDialog("Error al iniciar sesión", state.message)
                }
                else -> b.btnLogin.isEnabled = true
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}