package com.lecturaviva.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lecturaviva.app.domain.repository.AuthRepository
import com.lecturaviva.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    val isLoggedIn get() = authRepo.currentUser != null

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Rellena todos los campos antes de continuar.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            when (val r = authRepo.login(email, password)) {
                is Result.Success -> _authState.value = AuthState.Success
                is Result.Error   -> _authState.value = AuthState.Error(r.message)
                else -> {}
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Rellena todos los campos antes de continuar.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            when (val r = authRepo.register(name, email, password)) {
                is Result.Success -> _authState.value = AuthState.Registered
                is Result.Error   -> _authState.value = AuthState.Error(r.message)
                else -> {}
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Introduce tu correo electrónico.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            when (val r = authRepo.sendPasswordResetEmail(email)) {
                is Result.Success -> _authState.value = AuthState.ResetSent
                is Result.Error   -> _authState.value = AuthState.Error(r.message)
                else -> {}
            }
        }
    }

    fun resetState() { _authState.value = AuthState.Idle }
}

sealed class AuthState {
    object Idle       : AuthState()
    object Loading    : AuthState()
    object Success    : AuthState()
    object Registered : AuthState()
    object ResetSent  : AuthState()
    data class Error(val message: String) : AuthState()
}
