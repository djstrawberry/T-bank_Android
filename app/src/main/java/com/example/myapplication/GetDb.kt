package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.myapplication.DatabaseLibrary.Companion.DATABASE_NAME

class GetDb : Application() {
    companion object {
        private var instance: DatabaseLibrary? = null

        fun getDatabase(context: Context): DatabaseLibrary {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseLibrary::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}