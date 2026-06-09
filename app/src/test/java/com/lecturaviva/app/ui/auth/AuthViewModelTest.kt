package com.lecturaviva.app.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lecturaviva.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val authRepository: AuthRepository = mock()
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginConEmailVacioEmiteAuthStateError() {
        //Llamamos al login con el campo email vacío
        viewModel.login("", "password123")

        //Se comprueba que el estado es error
        val estado = viewModel.authState.value
        assertTrue(
            "El estado debe ser AuthState.Error",
            estado is AuthState.Error
        )

        //se comprueba que el mensaje es el que devuelve el ViewModel
        assertEquals(
            "Rellena todos los campos antes de continuar.",
            (estado as AuthState.Error).message
        )
        //comprobamos que Firebase no fue llamado en ningún momento
        verifyNoInteractions(authRepository)
    }

    @Test
    fun registroConContraseniaCortaEmiteError() {
        //llamamos a register cib cibtraseña de solo 3 caracteres
        viewModel.register("Marta", "marta@gmail.com", "123")

        //comprobamos que el estado es error
        val estado = viewModel.authState.value
        assertTrue(
            "El estado debe ser AuthState.Error",
            estado is AuthState.Error
        )
        assertEquals(
            "La contraseña debe tener al menos 6 caracteres.",
            (estado as AuthState.Error).message
        )
        //comprobamos que Firebase no fue llamado en ningún momento
        verifyNoInteractions(authRepository)
    }

    @Test
    fun loginConCredencialesIncorrectasEmiteError() = runBlocking{
        //configuramos el mock para que devuelva error de credenciales
        org.mockito.kotlin.whenever(
            authRepository.login("marta@gmail.com", "contraseniaincorrecta")
        ).thenReturn(com.lecturaviva.app.util.Result.Error("Correo o contraseña incorrectos."))

        //llamamos al login con credenciales incorrectas
        viewModel.login("marta@gmail.com", "contraseniaincorrecta")

        //comprobamos que el estado es error
        val estado = viewModel.authState.value
        assertTrue("El estado debe ser AuthState.Error", estado is AuthState.Error)
        assertEquals(
            "Correo o contraseña incorrectos.",
            (estado as AuthState.Error).message
        )
    }
}