package com.example.final_project.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.final_project.R
import com.example.final_project.adapter.TaskAdapter
import com.example.final_project.config.Database
import com.example.final_project.dao.TaskDao
import com.example.final_project.entity.Task
import com.example.final_project.model.ListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainFragment : Fragment(R.layout.main_fragment) {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskDao: TaskDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("userId", -1L)
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        val taskDatabase = Room.databaseBuilder(
            requireContext(),
            Database::class.java,
            "notes_database"
        ).build()
        taskDao = taskDatabase.taskDao()

        recyclerView = view.findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(emptyList()) { task ->
            navigateToTask(task.id ?: 0)
        }
        recyclerView.adapter = taskAdapter

        calendarView = view.findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth) // Adjusted to LocalDate
            loadTasksForDate(userId, selectedDate)
        }

        loadTasksForDate(userId, LocalDate.now())

        return view
    }

    private fun loadTasksForDate(userId: Long, date: LocalDate) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val tasks = taskDao.getTasksByDate(userId, date)

                val listItems = prepareListItems(tasks)
                withContext(Dispatchers.Main) {
                    taskAdapter.updateItems(listItems)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка при загрузке задач", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun prepareListItems(tasks: List<Task>): List<ListItem> {
        return tasks.groupBy {
            it.date?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "No Date"
        }.flatMap { (date, tasksForDate) ->
            val items = mutableListOf<ListItem>(ListItem.DateHeader(date))

            items.addAll(tasksForDate.map { task ->
                ListItem.TaskItem(task.copy(time = task.time))
            })

            items
        }
    }

    private fun navigateToTask(taskId: Long) {
        val bundle = Bundle().apply {
            putLong("taskId", taskId)
        }
        findNavController().navigate(R.id.action_mainFragment_to_taskItemFragment, bundle)
    }
}