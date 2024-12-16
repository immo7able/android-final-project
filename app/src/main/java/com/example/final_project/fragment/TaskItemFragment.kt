package com.example.final_project.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.final_project.R
import com.example.final_project.config.Database
import com.example.final_project.dao.TaskDao
import com.example.final_project.entity.Task
import com.example.final_project.entity.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class TaskItemFragment : Fragment() {

    private lateinit var taskDao: TaskDao
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var statusSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private val calendar: Calendar = Calendar.getInstance()
    private var userId: Long = -1
    private var taskId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.task_item_fragment, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getLong("userId", -1L)

        val taskDatabase = Room.databaseBuilder(
            requireContext(),
            Database::class.java,
            "notes_database"
        ).build()
        taskDao = taskDatabase.taskDao()

        titleEditText = rootView.findViewById(R.id.editTextTitle)
        descriptionEditText = rootView.findViewById(R.id.editTextDescription)
        dateEditText = rootView.findViewById(R.id.editTextDate)
        timeEditText = rootView.findViewById(R.id.editTextTime)
        statusSpinner = rootView.findViewById(R.id.spinnerStatus)
        saveButton = rootView.findViewById(R.id.buttonSave)
        deleteButton = rootView.findViewById(R.id.buttonDelete)

        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            TaskStatus.entries.toTypedArray()
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = statusAdapter

        taskId = arguments?.getLong("taskId") ?: 0

        if (taskId != 0L) {
            loadTask()
            deleteButton.visibility = View.VISIBLE
        }

        saveButton.setOnClickListener {
            saveTask()
        }

        deleteButton.setOnClickListener {
            deleteTask()
        }

        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        timeEditText.setOnClickListener {
            showTimePickerDialog()
        }

        return rootView
    }

    private fun loadTask() {
        lifecycleScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId)
            withContext(Dispatchers.Main) {
                task?.let {
                    titleEditText.setText(it.title)
                    descriptionEditText.setText(it.description)

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                    val date = it.date
                    val time = it.time

                    val dateStr = date?.let {
                        val dateInMillis = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        dateFormat.format(dateInMillis)
                    }

                    val timeStr = time?.let {
                        val timeInMillis = Date.from(it.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant())
                        timeFormat.format(timeInMillis)
                    }

                    dateEditText.setText(dateStr)
                    timeEditText.setText(timeStr)

                    statusSpinner.setSelection(it.status.ordinal)
                }
            }
        }
    }

    private fun saveTask() {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val dateInput = dateEditText.text.toString()
        val timeInput = timeEditText.text.toString()
        val status = TaskStatus.entries[statusSpinner.selectedItemPosition]

        if (title.isBlank() || description.isBlank() || dateInput.isBlank() || timeInput.isBlank()) {
            Toast.makeText(requireContext(), "Все поля должны быть заполнены!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            val date = dateFormat.parse(dateInput)
            var time = timeFormat.parse(timeInput)

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DATE, date.date)
                set(Calendar.HOUR_OF_DAY, time.hours)
                set(Calendar.MINUTE, time.minutes)
            }

            val localDate = LocalDate.ofInstant(calendar.toInstant(), ZoneId.systemDefault())
            val localTime = LocalTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault())

            val task = Task(
                id = if (taskId != 0L) taskId else null,
                title = title,
                description = description,
                date = localDate,
                time = localTime,
                userId = userId,
                status = status,
                isDeleted = false
            )

            lifecycleScope.launch(Dispatchers.IO) {
                if (taskId == 0L) {
                    taskDao.insertTask(task)
                } else {
                    taskDao.updateTask(task)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Задача сохранена", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Введите корректную дату и время!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask() {
        lifecycleScope.launch(Dispatchers.IO) {
            taskDao.deleteTaskById(taskId)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Задача удалена", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateEditText.setText(dateFormat.format(calendar.time))
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeEditText.setText(timeFormat.format(calendar.time))
        }

        TimePickerDialog(
            requireContext(),
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}
