package com.example.final_project.config

import android.app.Application

class TaskApplication : Application() {
    lateinit var db: Database

    override fun onCreate() {
        super.onCreate()
        //db = Room.databaseBuilder<Database>("notes_database").setDriver(BundledSQLiteDriver()).build()
    }
}