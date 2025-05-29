package com.taskive.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Task(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val datetime: String,
    val daysLeft: String,
    val description: String = "",
    val isCompleted: Boolean = false
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("taskive_tasks", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> get() = _tasks.toList()

    private val _completedTasks = mutableStateListOf<Task>()
    val completedTasks: List<Task> get() = _completedTasks.toList()

    // For dashboard to show only latest 3 tasks
    val recentTasks: List<Task>
        get() = _tasks.filter { !it.isCompleted }.take(3)

    private val _completedCount = mutableStateOf(sharedPreferences.getInt("completed_count", 0))
    val completedCount: State<Int> = _completedCount

    private val _showAddTaskDialog = mutableStateOf(false)
    val showAddTaskDialog: State<Boolean> = _showAddTaskDialog

    private val _selectedTask = mutableStateOf<Task?>(null)
    val selectedTask: State<Task?> = _selectedTask

    private val _showEditTaskDialog = mutableStateOf(false)
    val showEditTaskDialog: State<Boolean> = _showEditTaskDialog

    init {
        loadTasks()
        loadCompletedTasks()
    }

    private fun loadTasks() {
        val tasksJson = sharedPreferences.getString("tasks", "[]")
        _tasks.clear()
        _tasks.addAll(gson.fromJson(tasksJson, Array<Task>::class.java).filter { !it.isCompleted })
    }

    private fun loadCompletedTasks() {
        val completedTasksJson = sharedPreferences.getString("completed_tasks", "[]")
        _completedTasks.clear()
        _completedTasks.addAll(gson.fromJson(completedTasksJson, Array<Task>::class.java))
    }

    private fun saveTasks() {
        sharedPreferences.edit()
            .putString("tasks", gson.toJson(_tasks.toList()))
            .putInt("completed_count", _completedCount.value)
            .apply()
    }

    private fun saveCompletedTasks() {
        sharedPreferences.edit()
            .putString("completed_tasks", gson.toJson(_completedTasks.toList()))
            .apply()
    }

    fun openAddTaskDialog() {
        _showAddTaskDialog.value = true
    }

    fun dismissAddTaskDialog() {
        _showAddTaskDialog.value = false
    }

    fun selectTask(task: Task) {
        _selectedTask.value = task
    }

    fun openEditTaskDialog() {
        _showEditTaskDialog.value = true
    }

    fun dismissEditTaskDialog() {
        _showEditTaskDialog.value = false
        _selectedTask.value = null
    }

    fun addTask(title: String, datetime: String, daysLeft: String, description: String = "") {
        val dateTime = datetime.split(", ")
        val time = dateTime.getOrNull(0)
        val date = dateTime.getOrNull(1)

        val task = Task(
            title = title,
            datetime = datetime,
            daysLeft = calculateTaskStatus(date, time),
            description = description
        )
        _tasks.add(0, task)
        saveTasks()
    }

    fun updateTask(
        taskId: String,
        title: String,
        datetime: String,
        daysLeft: String,
        description: String,
        isCompleted: Boolean
    ) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            if (isCompleted) {
                val completedTask = _tasks[index].copy(isCompleted = true)
                _tasks.removeAt(index)
                _completedTasks.add(0, completedTask)
                _completedCount.value += 1
                saveTasks()
                saveCompletedTasks()
            } else {
                val dateTime = datetime.split(", ")
                val time = dateTime.getOrNull(0)
                val date = dateTime.getOrNull(1)

                val updatedTask = Task(
                    id = taskId,
                    title = title,
                    datetime = datetime,
                    daysLeft = calculateTaskStatus(date, time),
                    description = description,
                    isCompleted = false
                )
                _tasks[index] = updatedTask
                saveTasks()
            }
        }
        dismissEditTaskDialog()
    }

    private fun calculateTaskStatus(date: String?, time: String?): String {
        if (date == null || date == "Select Date") {
            if (time == null || time == "Select Time") {
                return "No Due Date"
            }
            return "Due Today"
        }

        val currentCalendar = Calendar.getInstance()
        val taskCalendar = Calendar.getInstance()

        try {
            // Parse the date first
            val dateParts = date.split("/")
            if (dateParts.size == 3) {
                val day = dateParts[0].toInt()
                val month = dateParts[1].toInt() - 1  // Calendar months are 0-based
                val year = dateParts[2].toInt()

                taskCalendar.set(Calendar.YEAR, year)
                taskCalendar.set(Calendar.MONTH, month)
                taskCalendar.set(Calendar.DAY_OF_MONTH, day)

                // Set time if available
                if (time != null && time != "Select Time") {
                    val timeParts = time.split(":")
                    if (timeParts.size == 2) {
                        taskCalendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                        taskCalendar.set(Calendar.MINUTE, timeParts[1].toInt())
                    }
                } else {
                    // If no time set, set to end of day
                    taskCalendar.set(Calendar.HOUR_OF_DAY, 23)
                    taskCalendar.set(Calendar.MINUTE, 59)
                }
            }

            // Reset seconds and milliseconds for both calendars
            currentCalendar.set(Calendar.SECOND, 0)
            currentCalendar.set(Calendar.MILLISECOND, 0)
            taskCalendar.set(Calendar.SECOND, 0)
            taskCalendar.set(Calendar.MILLISECOND, 0)

            // Compare with current time
            if (taskCalendar.before(currentCalendar)) {
                return "Due Date exceeded"
            }

            // Calculate days difference
            val diffInMillis = taskCalendar.timeInMillis - currentCalendar.timeInMillis
            val daysDiff = diffInMillis / (24 * 60 * 60 * 1000)

            return when {
                daysDiff == 0L -> "Due Today"
                daysDiff == 1L -> "1 Day left"
                daysDiff > 1L -> "$daysDiff Days left"
                else -> "Due Date exceeded"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Invalid Date Format"
        }
    }

    fun deleteTask(task: Task) {
        _tasks.remove(task)
        saveTasks()
        dismissEditTaskDialog()
    }
}
