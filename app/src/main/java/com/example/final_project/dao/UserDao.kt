package com.example.final_project.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.final_project.entity.Task
import com.example.final_project.entity.User

@Dao
interface UserDao {
    @Insert
    fun insert(user: User): Long

    @Update
    fun update(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUser(id: Long): User?

    @Query("SELECT * FROM users WHERE username = :username")
    fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>
}

