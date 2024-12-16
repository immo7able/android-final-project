package com.example.final_project.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String,
    val description: String,
    val date: LocalDate? = LocalDate.now(),
    val time: LocalTime? = LocalTime.now(),
    val userId: Long,
    val status: TaskStatus = TaskStatus.IN_PROGRESS,
    val isDeleted: Boolean = false
)

enum class TaskStatus {
    POSTPONED, IN_PROGRESS, NOT_COMPLETED
}
