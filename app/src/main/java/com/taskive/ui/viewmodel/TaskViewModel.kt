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
        var taskDeadline: Long? = null
        val dateTimeArray = datetime.split(", ")
        val calendar = Calendar.getInstance()

        if (dateTimeArray.size == 2) {
            val time = dateTimeArray[0]
            val date = dateTimeArray[1]

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            try {
                // Case 1: Only date selected (no time)
                if (date != "Select Date" && (time == "Select Time" || time.isEmpty())) {
                    calendar.time = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)!!
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    taskDeadline = calendar.timeInMillis
                }
                // Case 2: Only time selected (no date)
                else if (date == "Select Date" && time != "Select Time" && time.isNotEmpty()) {
                    val timeParts = time.split(":")
                    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                    taskDeadline = calendar.timeInMillis
                }
                // Case 3: Both date and time selected
                else if (date != "Select Date" && time != "Select Time" && time.isNotEmpty()) {
                    taskDeadline = sdf.parse("$date $time")?.time
                }
            } catch (e: Exception) {
                e.printStackTrace()
                taskDeadline = null
            }
        }

        val daysLeft = calculateRemainingTime(taskDeadline)

        // Assign pet if there's any kind of deadline
        val assignedPetId = if (taskDeadline != null && userViewModel.pets.isNotEmpty()) {
            userViewModel.pets.random().id
        } else null

        val task = Task(
            title = title,
            datetime = when {
                taskDeadline == null -> ""
                dateTimeArray[0] == "Select Time" || dateTimeArray[0].isEmpty() ->
                    "23:59, ${dateTimeArray[1]}"
                dateTimeArray[1] == "Select Date" ->
                    "${dateTimeArray[0]}, ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)}"
                else -> datetime
            },
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

        return when {
            diffMillis < 0 -> "Due Date Exceeded!"
            diffMillis < 60 * 1000 -> "Less than a minute left"
            diffMillis < 60 * 60 * 1000 -> "${diffMillis / (60 * 1000)} minutes left"
            diffMillis < 24 * 60 * 60 * 1000 -> "${diffMillis / (60 * 60 * 1000)} hours left"
            else -> "${diffMillis / (24 * 60 * 60 * 1000)} days left"
        }
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

                // Check if task was completed before deadline
                if (existingTask.deadline != null && System.currentTimeMillis() < existingTask.deadline) {
                    userViewModel.addXPAndCoins(20, 15) // Use userViewModel to update both XP and coins
                }

                saveTasks()
                saveCompletedTasks()
            } else {
                var taskDeadline: Long? = null
                val dateTimeArray = datetime.split(", ")
                val calendar = Calendar.getInstance()

                if (dateTimeArray.size == 2) {
                    val time = dateTimeArray[0]
                    val date = dateTimeArray[1]

                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                    try {
                        // Case 1: Only date selected (no time)
                        if (date != "Select Date" && (time == "Select Time" || time.isEmpty())) {
                            calendar.time = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)!!
                            calendar.set(Calendar.HOUR_OF_DAY, 23)
                            calendar.set(Calendar.MINUTE, 59)
                            taskDeadline = calendar.timeInMillis
                        }
                        // Case 2: Only time selected (no date)
                        else if (date == "Select Date" && time != "Select Time" && time.isNotEmpty()) {
                            val timeParts = time.split(":")
                            calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                            calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                            taskDeadline = calendar.timeInMillis
                        }
                        // Case 3: Both date and time selected
                        else if (date != "Select Date" && time != "Select Time" && time.isNotEmpty()) {
                            taskDeadline = sdf.parse("$date $time")?.time
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        taskDeadline = null
                    }
                }

                val updatedTask = existingTask.copy(
                    title = title,
                    datetime = when {
                        taskDeadline == null -> ""
                        dateTimeArray[0] == "Select Time" || dateTimeArray[0].isEmpty() ->
                            "23:59, ${dateTimeArray[1]}"
                        dateTimeArray[1] == "Select Date" ->
                            "${dateTimeArray[0]}, ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)}"
                        else -> datetime
                    },
                    daysLeft = calculateRemainingTime(taskDeadline),
                    description = description,
                    deadline = taskDeadline,
                    // Maintain or assign pet if deadline exists
                    assignedPetId = if (taskDeadline != null) {
                        existingTask.assignedPetId ?: if (userViewModel.pets.isNotEmpty()) {
                            userViewModel.pets.random().id
                        } else null
                    } else null
                )
                _tasks[index] = updatedTask
                saveTasks()
            }
        }
        dismissEditTaskDialog()
    }

    private fun updateTaskStatuses() {
        val currentTime = System.currentTimeMillis()
        var tasksUpdated = false

        _tasks.forEachIndexed { index, task ->
            if (!task.isCompleted && task.deadline != null) {
                // Update task status
                val newStatus = calculateRemainingTime(task.deadline)
                var updatedTask = task

                if (task.daysLeft != newStatus) {
                    updatedTask = task.copy(daysLeft = newStatus)
                    tasksUpdated = true
                }

                // Check if deadline is exceeded and has a pet assigned
                if (task.deadline < currentTime && task.assignedPetId != null) {
                    val timeSinceDeadline = currentTime - task.deadline
                    val daysLate = (timeSinceDeadline / (24 * 60 * 60 * 1000)).toInt()

                    // Calculate how many days worth of damage we need to apply
                    val daysToCharge = if (updatedTask.lastHealthReduction == 0L) {
                        maxOf(1, daysLate)
                    } else {
                        val timeSinceLastReduction = currentTime - updatedTask.lastHealthReduction
                        val daysSinceLastReduction = (timeSinceLastReduction / (24 * 60 * 60 * 1000)).toInt()
                        if (daysSinceLastReduction >= 1) daysSinceLastReduction else 0
                    }

                    if (daysToCharge > 0) {
                        // Reduce HP by 10 for each day late
                        userViewModel.reducePetHealth(task.assignedPetId, daysToCharge * 10)
                        updatedTask = updatedTask.copy(lastHealthReduction = currentTime)
                        tasksUpdated = true
                    }
                }

                if (tasksUpdated) {
                    _tasks[index] = updatedTask
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
                delay(10 * 1000) // Check every 10 seconds for more responsive updates
            }
        }
    }

    fun deleteTask(task: Task) {
        _tasks.remove(task)
        saveTasks()
        dismissEditTaskDialog()
    }

    fun completeTask(taskId: String) {
        val task = _tasks.find { it.id == taskId } ?: return
        val currentTime = System.currentTimeMillis()

        // Check if task has deadline and was completed before deadline
        if (task.deadline != null && currentTime <= task.deadline) {
            userViewModel.addXPAndCoins(20, 15) // Use userViewModel to update both XP and coins
        }

        val updatedTask = task.copy(isCompleted = true)
        _tasks.remove(task)
        _completedTasks.add(updatedTask)
        _completedCount.value = _completedCount.value + 1

        saveTasks()
        saveCompletedTasks()
        sharedPreferences.edit {
            putInt("completed_count", _completedCount.value)
        }
    }
}
