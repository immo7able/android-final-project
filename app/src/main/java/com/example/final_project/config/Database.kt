package com.example.final_project.config

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.final_project.converter.Converters
import com.example.final_project.dao.TaskDao
import com.example.final_project.dao.UserDao
import com.example.final_project.entity.Task
import com.example.final_project.entity.User

@Database(entities = [User::class, Task::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
}
