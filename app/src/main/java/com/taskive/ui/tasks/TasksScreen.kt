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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.taskive.DarkPurple
import com.taskive.ui.theme.MediumPurpleLight
import com.taskive.ui.theme.MediumPurpleDark
import com.taskive.ui.theme.Nunito
import com.taskive.ui.viewmodel.Task
import com.taskive.ui.viewmodel.TaskViewModel
import com.taskive.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    taskViewModel: TaskViewModel,
    userViewModel: UserViewModel
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
                                        userViewModel = userViewModel,
                                        onTaskClick = {
                                            taskViewModel.selectTask(task)
                                            taskViewModel.openEditTaskDialog()
                                        },
                                        onTaskComplete = {
                                            taskViewModel.updateTask(
                                                task.id,
                                                task.title,
                                                task.datetime,
                                                task.description,
                                                true
                                            )
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
                                        userViewModel = userViewModel,
                                        onTaskClick = { /* Completed tasks are not editable */ },
                                        onTaskComplete = { /* Completed tasks cannot be completed again */ }
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
                    onTaskCreate = { title, date, time, description, deadline ->
                        val datetime = "$time, $date"
                        taskViewModel.addTask(
                            title = title,
                            datetime = datetime,
                            description = description,
                            deadline = deadline
                        )
                    }
                )
            }

            if (taskViewModel.showEditTaskDialog.value) {
                EditTaskDialog(
                    task = taskViewModel.selectedTask.value!!,
                    onDismissRequest = { taskViewModel.dismissEditTaskDialog() },
                    onDeleteTask = { taskViewModel.deleteTask(it) },
                    onUpdateTask = { taskId, title, datetime, _, description, isCompleted ->
                        taskViewModel.updateTask(
                            taskId = taskId,
                            title = title,
                            datetime = datetime,
                            description = description,
                            isCompleted = isCompleted
                        )
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
    userViewModel: UserViewModel,
    onTaskClick: () -> Unit,
    onTaskComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onTaskClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MediumPurpleLight, MediumPurpleDark)
                    )
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    fontFamily = Nunito,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    task.description,
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            task.datetime,
                            fontFamily = Nunito,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Days Left",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            task.daysLeft,
                            fontFamily = Nunito,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Show pet if task has deadline and assigned pet
            if (task.deadline != null && task.assignedPetId != null) {
                val assignedPet = userViewModel.pets.find { it.id == task.assignedPetId }
                assignedPet?.let { pet ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(pet.getCurrentImage())
                                .crossfade(true)
                                .build(),
                            contentDescription = pet.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismissRequest: () -> Unit,
    onTaskCreate: (String, String, String, String, Long?) -> Unit
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
                            val deadline = if (selectedDateText != "Select Date" && selectedTimeText != "Select Time") {
                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                val dateTimeString = "${selectedDateText} ${selectedTimeText}"
                                sdf.parse(dateTimeString)?.time
                            } else null

                            onTaskCreate(
                                taskName,
                                selectedDateText,
                                selectedTimeText,
                                description,
                                deadline
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
                                selectedTimeText = String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d",
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                                showTimePicker = false
                            }) {
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
    val dateTimeArray = task.datetime.split(", ")
    var selectedTimeText by rememberSaveable { mutableStateOf(dateTimeArray.getOrNull(0) ?: "Select Time") }
    var selectedDateText by rememberSaveable { mutableStateOf(dateTimeArray.getOrNull(1) ?: "Select Date") }
    var isValidDateTime by remember { mutableStateOf(true) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)

    // Function to check if selected time is in the past
    fun isDateTimeCombinationValid(date: String, time: String): Boolean {
        if (date == "Select Date" || time == "Select Time") return true

        try {
            val calendar = Calendar.getInstance()
            if (date != "Select Date") {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                calendar.time = dateFormat.parse(date) ?: return false
            }

            if (time != "Select Time") {
                val timeParts = time.split(":")
                calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                calendar.set(Calendar.MINUTE, timeParts[1].toInt())
            }

            return calendar.timeInMillis > System.currentTimeMillis()
        } catch (e: Exception) {
            return false
        }
    }

    // Update validation whenever date or time changes
    LaunchedEffect(selectedDateText, selectedTimeText) {
        isValidDateTime = isDateTimeCombinationValid(selectedDateText, selectedTimeText)
    }

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
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date Selection with OutlinedTextField style
                OutlinedTextField(
                    value = selectedDateText,
                    onValueChange = { },
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, "Select date")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time Selection with OutlinedTextField style
                OutlinedTextField(
                    value = selectedTimeText,
                    onValueChange = { },
                    label = { Text("Time") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    enabled = false,
                    trailingIcon = {
                        Icon(Icons.Default.Schedule, "Select time")
                    }
                )

                if (!isValidDateTime && selectedDateText != "Select Date" && selectedTimeText != "Select Time") {
                    Text(
                        text = "Cannot set deadline in the past",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (taskName.isNotEmpty()) {
                            val datetime = "$selectedTimeText, $selectedDateText"
                            onUpdateTask(
                                task.id,
                                taskName,
                                datetime,
                                task.daysLeft,
                                description,
                                true // Mark as completed
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = taskName.isNotEmpty() && isValidDateTime,
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
                                val datetime = "$selectedTimeText, $selectedDateText"
                                onUpdateTask(
                                    task.id,
                                    taskName,
                                    datetime,
                                    task.daysLeft,
                                    description,
                                    false
                                )
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = taskName.isNotEmpty() && isValidDateTime
                    ) {
                        Text("Save", fontFamily = Nunito)
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            selectedDateText = sdf.format(calendar.time)
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
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
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                selectedTimeText = String.format("%02d:%02d",
                                    timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            }) {
                            Text("OK")
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
