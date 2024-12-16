package com.example.final_project.model

import com.example.final_project.entity.Task

sealed class ListItem {
    data class DateHeader(val date: String) : ListItem()
    data class TaskItem(val task: Task) : ListItem()
}
