package com.example.final_project.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.final_project.R
import com.example.final_project.model.ListItem
import com.example.final_project.adapter.TaskAdapter
import com.example.final_project.config.Database
import com.example.final_project.dao.TaskDao
import com.example.final_project.entity.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class TaskListFragment : Fragment() {

    private lateinit var taskDao: TaskDao
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton
    private var userId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.task_list_fragment, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getLong("userId", -1L)

        val taskDatabase = Room.databaseBuilder(
            requireContext(),
            Database::class.java,
            "notes_database"
        ).build()
        taskDao = taskDatabase.taskDao()

        recyclerView = rootView.findViewById(R.id.recyclerViewTasksList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(emptyList()) { task ->
            navigateToTask(task.id ?: 0)
        }
        recyclerView.adapter = taskAdapter

        fabAddTask = rootView.findViewById(R.id.fabAddTask)
        fabAddTask.setOnClickListener {
            navigateToTask(0)
        }

        loadTasks()

        return rootView
    }

    private fun loadTasks() {
        lifecycleScope.launch(Dispatchers.IO) {
            val tasks = taskDao.getTasksForUser(userId)
            val listItems = prepareListItems(tasks)
            withContext(Dispatchers.Main) {
                taskAdapter.updateItems(listItems)
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
        findNavController().navigate(R.id.action_taskListFragment_to_taskItemFragment, bundle)
    }
}
