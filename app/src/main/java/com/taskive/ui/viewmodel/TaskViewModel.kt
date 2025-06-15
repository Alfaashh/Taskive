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
import kotlin.math.abs

data class Task(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val datetime: String,
    var daysLeft: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val deadline: Long? = null,
    val assignedPetId: Int? = null,
    var lastHealthReduction: Long = 0 // Track when we last reduced health
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
        startPeriodicUpdates()
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

        val daysLeft = calculateRemainingTime(taskDeadline)

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

    private fun calculateRemainingTime(deadline: Long?): String {
        if (deadline == null) return "No Due Date"

        val currentTime = System.currentTimeMillis()
        val diffMillis = deadline - currentTime
        val diffDays = diffMillis / (24 * 60 * 60 * 1000)
        val diffHours = (diffMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val diffMinutes = (diffMillis % (60 * 60 * 1000)) / (60 * 1000)

        return when {
            diffMillis < 0 -> "Due Date Exceeded!"
            diffDays > 0 -> "$diffDays Days left"
            diffHours > 0 -> "$diffHours Hours left"
            diffMinutes > 0 -> "$diffMinutes Minutes left"
            else -> "Due Now!"
        }
    }

    private fun updateTaskStatuses() {
        val currentTime = System.currentTimeMillis()
        var tasksUpdated = false

        _tasks.forEachIndexed { index, task ->
            if (!task.isCompleted && task.deadline != null) {
                // Update task status
                val newStatus = calculateRemainingTime(task.deadline)
                if (task.daysLeft != newStatus) {
                    _tasks[index] = task.copy(daysLeft = newStatus)
                    tasksUpdated = true
                }

                // Check if deadline is exceeded and has a pet assigned
                if (task.assignedPetId != null && currentTime > task.deadline) {
                    val daysLate = ((currentTime - task.deadline) / (24 * 60 * 60 * 1000)).toInt()
                    val daysSinceLastReduction = ((currentTime - task.lastHealthReduction) / (24 * 60 * 60 * 1000)).toInt()

                    // Only reduce health if we haven't already done so for this day
                    if (daysLate > 0 && (task.lastHealthReduction == 0L || daysSinceLastReduction >= 1)) {
                        userViewModel.reducePetHealth(task.assignedPetId, 10) // Reduce 10 HP per day
                        _tasks[index] = task.copy(lastHealthReduction = currentTime)
                        tasksUpdated = true
                    }
                }
            }
        }

        if (tasksUpdated) {
            saveTasks()
        }
    }

    private fun startPeriodicUpdates() {
        viewModelScope.launch {
            while (true) {
                updateTaskStatuses()
                delay(30 * 1000) // Update every 30 seconds
            }
        }
    }

    fun deleteTask(task: Task) {
        _tasks.remove(task)
        saveTasks()
        dismissEditTaskDialog()
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
            val existingTask = _tasks[index]
            if (isCompleted) {
                val completedTask = existingTask.copy(isCompleted = true)
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

                val taskDeadline = if (date != null && time != null && date != "Select Date" && time != "Select Time") {
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    try {
                        sdf.parse("$date $time")?.time
                    } catch (e: Exception) {
                        null
                    }
                } else null

                val updatedTask = existingTask.copy(
                    title = title,
                    datetime = datetime,
                    daysLeft = calculateRemainingTime(taskDeadline),
                    description = description,
                    deadline = taskDeadline
                )
                _tasks[index] = updatedTask
                saveTasks()
            }
        }
        dismissEditTaskDialog()
    }
}
