package com.example.final_project.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.final_project.entity.Task
import java.time.LocalDate

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY date ASC")
    fun getAllTasks(): List<Task>

    @Insert
    fun insertTask(task: Task): Long

    @Update
    fun updateTask(task: Task)

    @Query("UPDATE tasks SET isDeleted = 1 WHERE id = :id")
    fun deleteTaskById(id: Long)

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND userId = :userId ORDER BY date ASC")
    fun getTasksForUser(userId: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND id = :taskId")
    fun getTaskById(taskId: Long): Task?

    @Query("SELECT * FROM tasks WHERE date = :date AND userId = :userId AND isDeleted = 0")
    fun getTasksByDate(userId: Long, date: LocalDate): List<Task>
}
