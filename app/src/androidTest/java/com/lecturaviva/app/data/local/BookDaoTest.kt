package com.lecturaviva.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lecturaviva.app.data.local.dao.BookDao
import com.lecturaviva.app.data.local.database.LecturaVivaDatabase
import com.lecturaviva.app.data.local.entities.BookEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    private lateinit var db: LecturaVivaDatabase
    private lateinit var bookDao: BookDao

    @Before
    fun setUp() {
        // Base de datos temporal en memoria — no afecta a los datos reales
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LecturaVivaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bookDao = db.bookDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertarYRecuperarLibro() = runBlocking {
        // Creamos un libro de prueba
        val libro = BookEntity(
            userId = "user123",
            title  = "El dia que dejo de nevar en Alaska",
            author = "Alice Kellen",
            status = "READ"
        )

        // Lo insertamos en Room
        bookDao.insertBook(libro)

        // Lo recuperamos con la misma consulta que usa la pantalla Mis libros
        val libros = bookDao.getAllBooks("user123").first()

        // Comprobamos que se guardó correctamente
        assertEquals("Debe haber exactamente 1 libro", 1, libros.size)
        assertEquals("El titulo debe coincidir",
            "El dia que dejo de nevar en Alaska", libros[0].title)
        assertEquals("El autor debe coincidir", "Alice Kellen", libros[0].author)
        assertEquals("El estado debe ser READ", "READ", libros[0].status)
    }
}