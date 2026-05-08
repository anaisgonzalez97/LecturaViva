package com.lecturaviva.app.ui.profile

import android.content.Context
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.lecturaviva.app.domain.model.Report
import com.lecturaviva.app.domain.repository.AuthRepository
import com.lecturaviva.app.domain.repository.BookRepository
import com.lecturaviva.app.domain.repository.ReportRepository
import com.lecturaviva.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val bookRepo: BookRepository,
    private val reportRepo: ReportRepository,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    val userId get() = auth.currentUser?.uid ?: "user"

    val currentUser get() = authRepo.currentUser

    private val _event = MutableStateFlow<ProfileEvent?>(null)
    val event: StateFlow<ProfileEvent?> = _event

    fun savePhotoUri(uri: String) {
        prefs.edit().putString("photo_uri_$userId", uri).apply()
    }

    fun getSavedPhotoUri(): String {
        return prefs.getString("photo_uri_$userId", "") ?: ""
    }

    fun updateProfile(name: String, email: String = "", newPassword: String = "", photoUri: String = "") {
        if (name.isBlank()) { _event.value = ProfileEvent.Error("El nombre no puede estar vacío."); return }
        viewModelScope.launch {
            if (photoUri.isNotBlank()) savePhotoUri(photoUri)
            val photoToSave = if (photoUri.isNotBlank()) photoUri else getSavedPhotoUri()
            when (val r = authRepo.updateProfile(name, photoToSave)) {
                is Result.Success -> {
                    if (newPassword.length >= 6) authRepo.updatePassword(newPassword)
                    _event.value = ProfileEvent.ProfileUpdated
                }
                is Result.Error -> _event.value = ProfileEvent.Error(r.message)
                else -> {}
            }
        }
    }

    fun sendReport(type: String, message: String) {
        viewModelScope.launch {
            val report = Report(
                userId    = userId,
                userEmail = auth.currentUser?.email ?: "",
                type      = type.ifBlank { "General" },
                message   = message
            )
            when (val r = reportRepo.sendReport(report)) {
                is Result.Success -> _event.value = ProfileEvent.ReportSent
                is Result.Error   -> _event.value = ProfileEvent.Error(r.message)
                else -> {}
            }
        }
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            bookRepo.syncFromFirestore(userId)
            _event.value = ProfileEvent.Synced
        }
    }

    fun logout() {
        authRepo.logout()
        _event.value = ProfileEvent.LoggedOut
    }

    fun clearEvent() { _event.value = null }
}

sealed class ProfileEvent {
    object ProfileUpdated : ProfileEvent()
    object LoggedOut      : ProfileEvent()
    object Synced         : ProfileEvent()
    object ReportSent     : ProfileEvent()
    data class Error(val msg: String) : ProfileEvent()
}