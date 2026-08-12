package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.dao.DatabaseLibrary
import com.example.myapplication.data.dao.GetDb
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(context: Context): DatabaseLibrary = GetDb.getDatabase(context)
}