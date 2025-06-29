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

    private val petSelectionHistory = mutableMapOf<Int, Int>() // petId to selection count
    private var totalPetSelections = 0

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

    private fun getNextPet(): Int? {
        val availablePets = userViewModel.pets.filter { it.isUsable() }
        if (availablePets.isEmpty()) return null

        // Reset history if all pets have been used equally
        if (petSelectionHistory.size == availablePets.size &&
            petSelectionHistory.values.all { it >= (totalPetSelections / availablePets.size) }) {
            petSelectionHistory.clear()
            totalPetSelections = 0
        }

        // Calculate weights based on usage history
        val weights = availablePets.map { pet ->
            val usageCount = petSelectionHistory[pet.id] ?: 0
            pet.id to (1.0 / (usageCount + 1.0))
        }.toMap()

        // Select pet using weighted probability
        val totalWeight = weights.values.sum()
        var random = Math.random() * totalWeight

        val selectedPet = weights.entries.first { (_, weight) ->
            random -= weight
            random <= 0
        }.key

        // Update history
        petSelectionHistory[selectedPet] = (petSelectionHistory[selectedPet] ?: 0) + 1
        totalPetSelections++

        return selectedPet
    }

    private fun isTaskDueToday(taskDeadline: Long): Boolean {
        val now = System.currentTimeMillis()
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return taskDeadline in startOfDay..endOfDay
    }

    private fun isTaskOverdue(taskDeadline: Long): Boolean {
        val now = System.currentTimeMillis()
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return taskDeadline < startOfDay
    }

    fun addTask(
        title: String,
        datetime: String,
        description: String = "",
        deadline: Long? = null
    ) {
        var taskDeadline = deadline
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
                    // Keep today's date, just update the time
                    val timeParts = time.split(":")
                    // Today's date is already set since we used Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
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

        // Only assign pet if there's a deadline and it's in the future
        val currentTime = System.currentTimeMillis()
        val assignedPetId = if (taskDeadline != null &&
                              taskDeadline > currentTime &&
                              userViewModel.pets.isNotEmpty()) {
            getNextPet()
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
            diffMillis < 60 * 1000 -> "Under a minute left"
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
        val taskIndex = _tasks.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val oldTask = _tasks[taskIndex]

            // Calculate deadline from datetime using the same logic as addTask
            var taskDeadline: Long? = null
            val dateTimeArray = datetime.split(", ")
            val calendar = Calendar.getInstance()

            if (dateTimeArray.size == 2) {
                val time = dateTimeArray[0]
                val date = dateTimeArray[1]
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                try {
                    // Calculate potential new deadline without setting it yet
                    val potentialDeadline = when {
                        // Case 1: Only date selected (no time) - preserve existing time if available
                        date != "Select Date" && (time == "Select Time" || time.isEmpty()) -> {
                            val newCalendar = Calendar.getInstance()
                            newCalendar.time = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)!!

                            // If there's an existing deadline, preserve its time
                            if (oldTask.deadline != null) {
                                val oldCalendar = Calendar.getInstance()
                                oldCalendar.timeInMillis = oldTask.deadline
                                newCalendar.set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                                newCalendar.set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                            } else {
                                newCalendar.set(Calendar.HOUR_OF_DAY, 23)
                                newCalendar.set(Calendar.MINUTE, 59)
                            }
                            newCalendar.timeInMillis
                        }
                        // Case 2: Only time selected (no date) - preserve the existing date
                        date == "Select Date" && time != "Select Time" && time.isNotEmpty() -> {
                            val newCalendar = Calendar.getInstance()
                            // If there's an existing deadline, use its date
                            if (oldTask.deadline != null) {
                                newCalendar.timeInMillis = oldTask.deadline
                            }
                            val timeParts = time.split(":")
                            newCalendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                            newCalendar.set(Calendar.MINUTE, timeParts[1].toInt())
                            newCalendar.timeInMillis
                        }
                        // Case 3: Both date and time selected
                        date != "Select Date" && time != "Select Time" && time.isNotEmpty() -> {
                            sdf.parse("$date $time")?.time
                        }
                        else -> oldTask.deadline // Preserve existing deadline if no changes
                    }

                    taskDeadline = potentialDeadline
                } catch (e: Exception) {
                    e.printStackTrace()
                    taskDeadline = oldTask.deadline
                }
            }

            // Keep the original datetime if trying to set a past time
            val finalDateTime = if (taskDeadline != null && taskDeadline < System.currentTimeMillis()) {
                oldTask.datetime
            } else {
                when {
                    taskDeadline == null -> ""
                    dateTimeArray[0] == "Select Time" || dateTimeArray[0].isEmpty() ->
                        "23:59, ${dateTimeArray[1]}"
                    dateTimeArray[1] == "Select Date" ->
                        "${dateTimeArray[0]}, ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)}"
                    else -> datetime
                }
            }

            // Assign pet if adding a deadline to a task that didn't have one
            val assignedPetId = when {
                oldTask.deadline == null && taskDeadline != null && userViewModel.pets.isNotEmpty() ->
                    userViewModel.pets.random().id
                else -> oldTask.assignedPetId
            }

            val daysLeft = calculateRemainingTime(taskDeadline)
            val newTask = Task(
                id = taskId,
                title = title,
                datetime = finalDateTime,
                daysLeft = daysLeft,
                description = description,
                isCompleted = isCompleted,
                deadline = taskDeadline,
                assignedPetId = assignedPetId,
                lastHealthReduction = oldTask.lastHealthReduction
            )

            if (!oldTask.isCompleted && isCompleted) {
                // Task was just completed
                _completedCount.value++
                userViewModel.updateCompletedTasks(_completedCount.value)
                sharedPreferences.edit {
                    putInt("completed_count", _completedCount.value)
                }
                // Remove from tasks and add to completed tasks
                _tasks.removeAt(taskIndex)
                _completedTasks.add(newTask)
                saveTasks()
                saveCompletedTasks()
                // Add coins and XP reward (15 coins)
                userViewModel.addCoins(15)
                userViewModel.addXPAndCoins(20, 0) // Only add XP, no additional coins
                dismissEditTaskDialog() // Auto-dismiss dialog after completion
            } else {
                // Just updating task details without changing completion status
                if (isCompleted) {
                    val completedTaskIndex = _completedTasks.indexOfFirst { it.id == taskId }
                    if (completedTaskIndex != -1) {
                        _completedTasks[completedTaskIndex] = newTask
                        saveCompletedTasks()
                    }
                } else {
                    _tasks[taskIndex] = newTask
                    saveTasks()
                }
                dismissEditTaskDialog() // Also dismiss dialog after regular updates
            }
        } else {
            dismissEditTaskDialog() // Dismiss dialog if task not found
        }
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

                    // Initial damage when task first becomes late
                    if (updatedTask.lastHealthReduction == 0L) {
                        userViewModel.reducePetHealth(task.assignedPetId, 10) // Initial 10 HP reduction
                        updatedTask = updatedTask.copy(lastHealthReduction = currentTime)
                        tasksUpdated = true
                    }

                    // Additional daily damage
                    if (daysLate > 0) {
                        val timeSinceLastReduction = currentTime - updatedTask.lastHealthReduction
                        val daysSinceLastReduction = (timeSinceLastReduction / (24 * 60 * 60 * 1000)).toInt()

                        if (daysSinceLastReduction >= 1) {
                            userViewModel.reducePetHealth(task.assignedPetId, daysSinceLastReduction * 10)
                            updatedTask = updatedTask.copy(lastHealthReduction = currentTime)
                            tasksUpdated = true
                        }
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
