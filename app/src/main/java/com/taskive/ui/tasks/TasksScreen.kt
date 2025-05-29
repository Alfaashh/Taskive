package com.taskive.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskive.DarkPurple
import com.taskive.ui.theme.MediumPurpleLight
import com.taskive.ui.theme.MediumPurpleDark
import com.taskive.ui.theme.Nunito
import com.taskive.ui.viewmodel.Task
import com.taskive.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    taskViewModel: TaskViewModel = viewModel()
) {
    val backgroundColor = Color(0xFFF5F5F5)
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(MediumPurpleLight, MediumPurpleDark)
    )

    var selectedTab by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { taskViewModel.openAddTaskDialog() },
                    containerColor = DarkPurple,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, "Add new task")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Tasks",
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = DarkPurple
                    )
                )

                // Custom Tab Layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        TabButton(
                            text = "My Tasks",
                            isSelected = selectedTab == 0,
                            gradientBrush = gradientBrush,
                            onClick = { selectedTab = 0 }
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        TabButton(
                            text = "Completed Tasks",
                            isSelected = selectedTab == 1,
                            gradientBrush = gradientBrush,
                            onClick = { selectedTab = 1 }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> {
                        if (taskViewModel.tasks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No tasks yet. Click + to add your first task!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray,
                                    fontFamily = Nunito
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                items(taskViewModel.tasks) { task ->
                                    TaskCard(
                                        task = task,
                                        gradientBrush = Brush.verticalGradient(
                                            colors = listOf(MediumPurpleLight, MediumPurpleDark)
                                        ),
                                        onClick = {
                                            taskViewModel.selectTask(task)
                                            taskViewModel.openEditTaskDialog()
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                    1 -> {
                        if (taskViewModel.completedTasks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No completed tasks yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray,
                                    fontFamily = Nunito
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                items(taskViewModel.completedTasks) { task ->
                                    TaskCard(
                                        task = task,
                                        gradientBrush = Brush.verticalGradient(
                                            colors = listOf(MediumPurpleLight, MediumPurpleDark)
                                        ),
                                        onClick = {}  // Completed tasks are not editable
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (taskViewModel.showAddTaskDialog.value) {
                AddTaskDialog(
                    onDismissRequest = {
                        taskViewModel.dismissAddTaskDialog()
                    },
                    onTaskCreate = { title, date, time, description ->
                        val datetime = "$time, $date"
                        // Calculate days left based on the selected date
                        val daysLeft = calculateDaysLeft(date)
                        taskViewModel.addTask(title, datetime, "$daysLeft days left", description)
                    }
                )
            }

            if (taskViewModel.showEditTaskDialog.value) {
                EditTaskDialog(
                    task = taskViewModel.selectedTask.value!!,
                    onDismissRequest = { taskViewModel.dismissEditTaskDialog() },
                    onDeleteTask = { taskViewModel.deleteTask(it) },
                    onUpdateTask = { taskId, title, datetime, daysLeft, description, isCompleted ->
                        taskViewModel.updateTask(taskId, title, datetime, daysLeft, description, isCompleted)
                    }
                )
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    gradientBrush: Brush,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isSelected) gradientBrush else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else DarkPurple,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold
            )
        }
    }
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
        // Parse the date
        val dateParts = date.split("/")
        if (dateParts.size == 3) {
            val day = dateParts[0].toInt()
            val month = dateParts[1].toInt() - 1  // Calendar months 0-based
            val year = dateParts[2].toInt()

            taskCalendar.set(Calendar.YEAR, year)
            taskCalendar.set(Calendar.MONTH, month)
            taskCalendar.set(Calendar.DAY_OF_MONTH, day)

            // Set time if available
            if (time != null && time != "Select Time") {
                val timeParts = time.split(":")
                if (timeParts.size == 2) {
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()
                    taskCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    taskCalendar.set(Calendar.MINUTE, minute)
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

@Composable
fun TaskCard(
    task: Task,
    gradientBrush: Brush,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = Nunito
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Only show datetime if it's not "Select Date" or "Select Time"
                val dateTime = task.datetime.split(", ")
                val time = dateTime.getOrNull(0)?.takeIf { it != "Select Time" }
                val date = dateTime.getOrNull(1)?.takeIf { it != "Select Date" }

                if (time != null || date != null) {
                    Text(
                        text = buildString {
                            time?.let { append(it) }
                            if (time != null && date != null) append(", ")
                            date?.let { append(it) }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontFamily = Nunito
                    )
                }

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = Nunito
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = calculateTaskStatus(date, time),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismissRequest: () -> Unit,
    onTaskCreate: (String, String, String, String) -> Unit
) {
    var taskName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedDateText by rememberSaveable { mutableStateOf("Select Date") }
    var selectedTimeText by rememberSaveable { mutableStateOf("Select Time") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Add New Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Nunito,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name", fontFamily = Nunito) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selection
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedDateText, fontFamily = Nunito)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Selection
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedTimeText, fontFamily = Nunito)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)", fontFamily = Nunito) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (taskName.isNotEmpty()) {
                            val datetime = if (selectedDateText != "Select Date" && selectedTimeText != "Select Time") {
                                "$selectedTimeText, $selectedDateText"
                            } else {
                                ""
                            }
                            val daysLeft = if (selectedDateText != "Select Date") {
                                calculateDaysLeft(selectedDateText)
                            } else {
                                ""
                            }
                            onTaskCreate(
                                taskName,
                                selectedDateText,
                                selectedTimeText,
                                description
                            )
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = taskName.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkPurple,
                        contentColor = Color.White
                    )
                ) {
                    Text("Create Task", fontFamily = Nunito)
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        selectedDateText = sdf.format(calendar.time)
                    }
                }) {
                    Text("OK", fontFamily = Nunito)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontFamily = Nunito)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Select Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Nunito
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel", fontFamily = Nunito)
                        }
                        TextButton(
                            onClick = {
                                showTimePicker = false
                                val newTimeText = String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d",
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                                selectedTimeText = newTimeText
                            }
                        ) {
                            Text("OK", fontFamily = Nunito)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismissRequest: () -> Unit,
    onDeleteTask: (Task) -> Unit,
    onUpdateTask: (String, String, String, String, String, Boolean) -> Unit
) {
    var taskName by rememberSaveable { mutableStateOf(task.title) }
    var description by rememberSaveable { mutableStateOf(task.description) }

    // Extract date and time from datetime string
    val dateTimeArray = task.datetime.split(", ")
    var selectedTimeText by rememberSaveable { mutableStateOf(dateTimeArray.getOrNull(0) ?: "Select Time") }
    var selectedDateText by rememberSaveable { mutableStateOf(dateTimeArray.getOrNull(1) ?: "Select Date") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Task",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Nunito
                    )
                    IconButton(
                        onClick = {
                            onDeleteTask(task)
                            onDismissRequest()
                        }
                    ) {
                        Icon(Icons.Default.Delete, "Delete task", tint = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name", fontFamily = Nunito) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selection
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedDateText, fontFamily = Nunito)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Selection
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedTimeText, fontFamily = Nunito)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontFamily = Nunito) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Complete Task Button
                Button(
                    onClick = {
                        val datetime = "$selectedTimeText, $selectedDateText"
                        onUpdateTask(
                            task.id,
                            taskName,
                            datetime,
                            task.daysLeft,
                            description,
                            true // Mark as completed
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Complete Task", fontFamily = Nunito)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontFamily = Nunito)
                    }
                    OutlinedButton(
                        onClick = {
                            if (taskName.isNotEmpty()) {
                                val datetime = if (selectedDateText != "Select Date" && selectedTimeText != "Select Time") {
                                    "$selectedTimeText, $selectedDateText"
                                } else {
                                    ""
                                }
                                val daysLeft = if (selectedDateText != "Select Date") {
                                    calculateDaysLeft(selectedDateText)
                                } else {
                                    ""
                                }
                                onUpdateTask(
                                    task.id,
                                    taskName,
                                    datetime,
                                    daysLeft,
                                    description,
                                    false
                                )
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = taskName.isNotEmpty()
                    ) {
                        Text("Save", fontFamily = Nunito)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        selectedDateText = sdf.format(calendar.time)
                    }
                }) {
                    Text("OK", fontFamily = Nunito)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontFamily = Nunito)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Select Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Nunito
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel", fontFamily = Nunito)
                        }
                        TextButton(
                            onClick = {
                                showTimePicker = false
                                val newTimeText = String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d",
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                                selectedTimeText = newTimeText
                            }
                        ) {
                            Text("OK", fontFamily = Nunito)
                        }
                    }
                }
            }
        }
    }
}

fun calculateDaysLeft(dateString: String): String {
    try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val selectedDate = sdf.parse(dateString) ?: return ""
        val currentDate = Calendar.getInstance().time
        val diff = selectedDate.time - currentDate.time
        val days = (diff / (24 * 60 * 60 * 1000)).toInt()

        return when {
            days < 0 -> "Due date exceeded"
            days == 0 -> "Due Today"
            else -> "$days days left"
        }
    } catch (e: Exception) {
        return ""
    }
}
