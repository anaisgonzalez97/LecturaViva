package com.lecturaviva.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lecturaviva.app.data.local.dao.BookDao
import com.lecturaviva.app.data.local.dao.NoteDao
import com.lecturaviva.app.data.local.entities.BookEntity
import com.lecturaviva.app.data.local.entities.NoteEntity

@Database(
    entities     = [BookEntity::class, NoteEntity::class],
    version      = 2,
    exportSchema = false
)
abstract class LecturaVivaDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun noteDao(): NoteDao

    companion object {
        // Migración 1→2: recrear tabla notes con chapter y page sin DEFAULT
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Guardar datos existentes
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS notes_backup (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        bookId INTEGER NOT NULL,
                        text TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("INSERT INTO notes_backup SELECT id, bookId, text, createdAt FROM notes")

                // Borrar tabla antigua
                db.execSQL("DROP TABLE IF EXISTS notes")

                // Crear tabla nueva con chapter y page SIN DEFAULT (undefined)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS notes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        bookId INTEGER NOT NULL,
                        text TEXT NOT NULL,
                        chapter INTEGER NOT NULL,
                        page INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(bookId) REFERENCES books(id) ON DELETE CASCADE
                    )
                """)

                // Restaurar datos con chapter=0 y page=0 para las notas antiguas
                db.execSQL("""
                    INSERT INTO notes (id, bookId, text, chapter, page, createdAt)
                    SELECT id, bookId, text, 0, 0, createdAt FROM notes_backup
                """)

                // Crear índice
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_bookId ON notes(bookId)")

                // Limpiar backup
                db.execSQL("DROP TABLE IF EXISTS notes_backup")
            }
        }
    }
}