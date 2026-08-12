package com.example.myapplication.di

import com.example.myapplication.data.repositories.GoogleBooksRepositoryImpl
import com.example.myapplication.data.repositories.LibraryRepositoryImpl
import com.example.myapplication.domain.repositories.GoogleBooksRepository
import com.example.myapplication.domain.repositories.LibraryRepository
import dagger.Binds
import dagger.Module

@Module
interface RepositoryModule {
    @Binds
    fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    fun bindGoogleBooksRepository(impl: GoogleBooksRepositoryImpl): GoogleBooksRepository
}