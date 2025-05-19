package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.dao.DatabaseLibrary
import com.example.myapplication.presentation.MyApplication
import com.example.myapplication.presentation.activities.MainActivity
import com.example.myapplication.presentation.fragments.LibraryFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, RepositoryModule::class, UseCaseModule::class])
interface AppComponent {
    fun inject(application: MyApplication)
    fun inject(activity: MainActivity)

    fun inject(fragment: LibraryFragment)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}