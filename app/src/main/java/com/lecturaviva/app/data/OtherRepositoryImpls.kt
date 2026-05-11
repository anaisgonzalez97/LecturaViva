package com.lecturaviva.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.lecturaviva.app.data.local.dao.NoteDao
import com.lecturaviva.app.data.local.entities.NoteEntity
import com.lecturaviva.app.data.remote.api.OpenLibraryApi
import com.lecturaviva.app.domain.model.Note
import com.lecturaviva.app.domain.model.User
import com.lecturaviva.app.domain.repository.AuthRepository
import com.lecturaviva.app.domain.repository.NoteRepository
import com.lecturaviva.app.domain.repository.SearchRepository
import com.lecturaviva.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ── Auth ─────────────────────────────────────────────────────────────────
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: User?
        get() = auth.currentUser?.let {
            User(uid = it.uid, name = it.displayName ?: "", email = it.email ?: "", photoUrl = it.photoUrl?.toString() ?: "")
        }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val u = result.user!!
        Result.Success(User(uid = u.uid, name = u.displayName ?: "", email = u.email ?: ""))
    }.getOrElse { Result.Error(traducirError(it.message)) }

    override suspend fun register(name: String, email: String, password: String): Result<User> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val u = result.user!!
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        u.updateProfile(profileUpdates).await()
        Result.Success(User(uid = u.uid, name = name, email = u.email ?: ""))
    }.getOrElse { Result.Error(traducirError(it.message)) }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
        Result.Success(Unit)
    }.getOrElse { e ->
        val msg = when {
            e.message?.contains("badly formatted") == true  -> "El formato del correo no es válido."
            e.message?.contains("user-not-found") == true   -> "No existe ninguna cuenta con ese correo."
            e.message?.contains("network") == true          -> "Sin conexión. Inténtalo de nuevo."
            else -> "Error: ${e.message}"
        }
        Result.Error(msg)
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        auth.currentUser?.updatePassword(newPassword)?.await()
    }.fold({ Result.Success(Unit) }, { Result.Error(it.message ?: "Error al cambiar la contraseña") })

    override suspend fun updateProfile(name: String, photoUrl: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: return Result.Error("No hay usuario activo")
        val req = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .also { if (photoUrl.isNotEmpty()) it.setPhotoUri(android.net.Uri.parse(photoUrl)) }
            .build()
        user.updateProfile(req).await()
        Result.Success(Unit)
    }.getOrElse { Result.Error(it.message ?: "Error al actualizar perfil") }

    override fun logout() { auth.signOut() }
}
private fun traducirError(message: String?): String {
    return when {
        message == null                               -> "Error desconocido."
        message.contains("badly formatted")           -> "El formato del correo no es válido."
        message.contains("INVALID_LOGIN_CREDENTIALS") -> "Correo o contraseña incorrectos."
        message.contains("wrong-password")            -> "La contraseña es incorrecta."
        message.contains("invalid-credential")        -> "Correo o contraseña incorrectos."
        message.contains("weak-password")             -> "La contraseña es demasiado débil."
        message.contains("too-many-requests")         -> "Demasiados intentos. Espera unos minutos."
        message.contains("network")                   -> "Sin conexión a internet."
        message.contains("user-disabled")             -> "Esta cuenta ha sido desactivada."
        message.contains("already in use")          -> "Ya existe una cuenta con ese correo."
        message.contains("supplied auth credential is incorrect") -> "Correo o contraseña incorrectos."
        message.contains("malformed or has expired")              -> "Correo o contraseña incorrectos."


        else                                          -> "Error al conectar. Inténtalo de nuevo."
    }
}

// ── Notes ─────────────────────────────────────────────────────────────────
@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotesByBook(bookId: Long): Flow<List<Note>> =
        noteDao.getNotesByBook(bookId).map { it.map(NoteEntity::toDomain) }

    override suspend fun addNote(note: Note): Long =
        noteDao.insertNote(NoteEntity.fromDomain(note))

    override suspend fun deleteNote(note: Note) =
        noteDao.deleteNote(NoteEntity.fromDomain(note))
}

// ── Search ────────────────────────────────────────────────────────────────
@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val api: OpenLibraryApi
) : SearchRepository {

    override suspend fun searchOpenLibrary(query: String): Result<List<com.lecturaviva.app.domain.model.Book>> =
        runCatching {
            val response = api.searchBooks(query)
            Result.Success(response.docs.map { it.toDomain() })
        }.getOrElse { Result.Error(it.message ?: "Error en la búsqueda") }
}