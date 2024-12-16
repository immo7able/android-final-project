package com.example.final_project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.final_project.R
import com.example.final_project.entity.Task
import com.example.final_project.model.ListItem
import java.time.format.DateTimeFormatter

class TaskAdapter(
    private var items: List<ListItem>,
    private val onClick: (Task) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DATE_VIEW_TYPE = 0
    private val TASK_VIEW_TYPE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DATE_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
                DateViewHolder(view)
            }

            TASK_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
                TaskViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.DateHeader -> (holder as DateViewHolder).bind(item.date)
            is ListItem.TaskItem -> (holder as TaskViewHolder).bind(item.task, onClick)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.DateHeader -> DATE_VIEW_TYPE
            is ListItem.TaskItem -> TASK_VIEW_TYPE
        }
    }

    fun updateItems(newItems: List<ListItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateTextView: TextView = view.findViewById(R.id.textViewDate)

        fun bind(date: String) {
            dateTextView.text = date
        }
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val taskTitle: TextView = view.findViewById(R.id.textViewTaskTitle)

        fun bind(task: Task, onClick: (Task) -> Unit) {
            val timeString = task.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "No Time"
            taskTitle.text = "${timeString} ${task.title}"
            itemView.setOnClickListener { onClick(task) }
        }
    }
}






