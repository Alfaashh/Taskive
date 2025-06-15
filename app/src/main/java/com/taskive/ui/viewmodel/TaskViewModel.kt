package com.taskive.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.edit
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

data class Task(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val datetime: String,
    val daysLeft: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val deadline: Long? = null,
    val assignedPetId: Int? = null
)

class TaskViewModel(
    application: Application,
    private val storeViewModel: StoreViewModel,
    private val userViewModel: UserViewModel
) : AndroidViewModel(application) {
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
        startDeadlineChecking()
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
        sharedPreferences.edit {
            putString("tasks", gson.toJson(_tasks.toList()))
            putInt("completed_count", _completedCount.value)
        }
    }

    private fun saveCompletedTasks() {
        sharedPreferences.edit {
            putString("completed_tasks", gson.toJson(_completedTasks.toList()))
        }
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

    fun addTask(
        title: String,
        datetime: String,
        description: String = "",
        deadline: Long? = null
    ) {
        val dateTimeArray = datetime.split(", ")
        val time = dateTimeArray.getOrNull(0)
        val date = dateTimeArray.getOrNull(1)

        val taskDeadline = if (date != null && time != null && date != "Select Date" && time != "Select Time") {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            try {
                sdf.parse("$date $time")?.time
            } catch (e: Exception) {
                null
            }
        } else null

        val daysLeft = calculateTaskStatus(date, time)

        // Only assign pet if there's a deadline
        val assignedPetId = if (taskDeadline != null && userViewModel.pets.isNotEmpty()) {
            userViewModel.pets.random().id
        } else null

        val task = Task(
            title = title,
            datetime = datetime,
            daysLeft = daysLeft,
            description = description,
            deadline = taskDeadline,
            assignedPetId = assignedPetId
        )

        _tasks.add(task)
        saveTasks()
    }

    fun updateTask(
        taskId: String,
        title: String,
        datetime: String,
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
                storeViewModel.addCoins(10) // Reward 10 coins for completing a task
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

    private fun checkTaskDeadlines() {
        val currentTime = System.currentTimeMillis()
        _tasks.forEach { task ->
            if (!task.isCompleted && task.deadline != null && task.assignedPetId != null) {
                if (currentTime > task.deadline) {
                    val daysLate = ((currentTime - task.deadline) / (24 * 60 * 60 * 1000)).toInt()
                    if (daysLate > 0) {
                        userViewModel.reducePetHealth(task.assignedPetId, daysLate * 10)

                        // Update task's days left status
                        val index = _tasks.indexOf(task)
                        if (index != -1) {
                            _tasks[index] = task.copy(daysLeft = "Due date exceeded")
                            saveTasks()
                        }
                    }
                }
            }
        }
    }

    private fun startDeadlineChecking() {
        viewModelScope.launch {
            while (true) {
                checkTaskDeadlines()
                delay(60 * 1000) // Check every minute instead of every hour
            }
        }
    }
}
