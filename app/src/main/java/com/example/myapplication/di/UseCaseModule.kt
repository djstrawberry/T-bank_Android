package com.example.myapplication.di

import com.example.myapplication.domain.repositories.GoogleBooksRepository
import com.example.myapplication.domain.repositories.LibraryRepository
import com.example.myapplication.domain.usecases.AddItemUseCase
import com.example.myapplication.domain.usecases.GetLibraryItemsUseCase
import com.example.myapplication.domain.usecases.SearchBooksUseCase
import dagger.Module
import dagger.Provides

@Module
class UseCaseModule {
    @Provides
    fun provideGetLibraryItemsUseCase(repository: LibraryRepository): GetLibraryItemsUseCase {
        return GetLibraryItemsUseCase(repository)
    }

    @Provides
    fun provideAddItemUseCase(repository: LibraryRepository): AddItemUseCase {
        return AddItemUseCase(repository)
    }

    @Provides
    fun provideSearchBooksUseCase(repository: GoogleBooksRepository): SearchBooksUseCase {
        return SearchBooksUseCase(repository)
    }
}