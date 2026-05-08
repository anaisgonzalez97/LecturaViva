package com.lecturaviva.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lecturaviva.app.data.*
import com.lecturaviva.app.data.local.dao.BookDao
import com.lecturaviva.app.data.local.dao.NoteDao
import com.lecturaviva.app.data.local.database.LecturaVivaDatabase
import com.lecturaviva.app.data.remote.api.OpenLibraryApi
import com.lecturaviva.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Room ─────────────────────────────────────────────────────────────
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): LecturaVivaDatabase =
        Room.databaseBuilder(ctx, LecturaVivaDatabase::class.java, "lectura_viva.db")
            .addMigrations(LecturaVivaDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideBookDao(db: LecturaVivaDatabase): BookDao = db.bookDao()
    @Provides fun provideNoteDao(db: LecturaVivaDatabase): NoteDao = db.noteDao()

    // ── Firebase ─────────────────────────────────────────────────────────
    @Provides @Singleton fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides @Singleton fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // ── Retrofit ──────────────────────────────────────────────────────────
    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun provideOpenLibraryApi(retrofit: Retrofit): OpenLibraryApi =
        retrofit.create(OpenLibraryApi::class.java)

    // ── Repositories ──────────────────────────────────────────────────────
    @Provides @Singleton
    fun provideBookRepository(bookDao: BookDao, firestore: FirebaseFirestore): BookRepository =
        BookRepositoryImpl(bookDao, firestore)

    @Provides @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository =
        NoteRepositoryImpl(noteDao)

    @Provides @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository =
        AuthRepositoryImpl(auth)

    @Provides @Singleton
    fun provideSearchRepository(api: OpenLibraryApi): SearchRepository =
        SearchRepositoryImpl(api)

}