package com.example.myapplication.presentation

import android.app.Application
import com.example.myapplication.data.dao.DatabaseLibrary
import com.example.myapplication.data.dao.GetDb
import com.example.myapplication.data.repositories.GoogleBooksRepositoryImpl
import com.example.myapplication.data.repositories.LibraryRepositoryImpl
import com.example.myapplication.domain.repositories.GoogleBooksRepository
import com.example.myapplication.domain.repositories.LibraryRepository
import com.example.myapplication.domain.usecases.AddItemUseCase
import com.example.myapplication.domain.usecases.GetLibraryItemsUseCase
import com.example.myapplication.domain.usecases.SearchBooksUseCase

class MyApplication : Application() {
    private lateinit var db: DatabaseLibrary
    lateinit var libraryRepository: LibraryRepository
    private lateinit var googleBooksRepository: GoogleBooksRepository

    lateinit var getLibraryItemsUseCase: GetLibraryItemsUseCase
    lateinit var addItemUseCase: AddItemUseCase
    private lateinit var searchBooksUseCase: SearchBooksUseCase

    override fun onCreate() {
        super.onCreate()

        db = GetDb.getDatabase(this)

        libraryRepository = LibraryRepositoryImpl(db)
        googleBooksRepository = GoogleBooksRepositoryImpl(this)

        getLibraryItemsUseCase = GetLibraryItemsUseCase(libraryRepository)
        addItemUseCase = AddItemUseCase(libraryRepository)
        searchBooksUseCase = SearchBooksUseCase(googleBooksRepository)
    }
}