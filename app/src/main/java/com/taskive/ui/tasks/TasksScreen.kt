package com.taskive.ui.tasks

import android.os.Build
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.ShaderBrush // Tidak dipakai jika blur tanpa edgeTreatment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import com.taskive.NAV_ARG_SHOW_DIALOG // Impor konstanta dari MainActivity
// import com.taskive.Screen // Tidak dipakai langsung di sini jika NAV_ARG_SHOW_DIALOG diimpor
import java.text.SimpleDateFormat
import java.util.*

// TasksScreen Composable (dari respons sebelumnya, tidak ada perubahan di sini)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TasksViewModel = viewModel()
) {
    val showDialogState by viewModel.showAddTaskDialogFlow.collectAsState()
    Log.d("TasksScreen", "Recomposing. showDialog state from ViewModel: $showDialogState")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val showDialogFromNavArg = navBackStackEntry?.arguments?.getBoolean(NAV_ARG_SHOW_DIALOG) ?: false

    LaunchedEffect(navBackStackEntry) {
        Log.d("TasksScreen", "LaunchedEffect: current NBE changed. showDialogFromNavArg from NavHost args: $showDialogFromNavArg. VM instance: $viewModel")
        viewModel.processShowDialogNavArgument(showDialogFromNavArg)

        if (showDialogFromNavArg) {
            val currentRouteBase = navController.currentDestination?.route?.substringBefore('?') ?: navController.graph.findStartDestination().route!! // Fallback ke start destination jika route null
            if (navController.currentBackStackEntry?.arguments?.getBoolean(NAV_ARG_SHOW_DIALOG) == true) {
                Log.d("TasksScreen", "Consuming nav arg by navigating to $currentRouteBase?$NAV_ARG_SHOW_DIALOG=false")
                navController.navigate("$currentRouteBase?$NAV_ARG_SHOW_DIALOG=false") {
                    val currentNavEntryId = navBackStackEntry?.destination?.id
                    if (currentNavEntryId != null) {
                        popUpTo(currentNavEntryId) {
                            inclusive = true
                        }
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .then(
                    if (showDialogState && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(radius = 10.dp)
                    } else if (showDialogState) {
                        Modifier.background(Color.Black.copy(alpha = 0.3f))
                    } else {
                        Modifier
                    }
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Daftar Tugas Akan Tampil di Sini", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                Log.d("TasksScreen", "Test Munculkan Dialog Manual button clicked")
                viewModel.requestShowDialogExplicitly()
            }) {
                Text("Test Munculkan Dialog Manual")
            }
        }

        if (showDialogState) {
            Log.d("TasksScreen", "Rendering AddTaskDialog because showDialogState is true")
            AddTaskDialog(
                onDismissRequest = {
                    Log.d("TasksScreen", "AddTaskDialog onDismissRequest by user")
                    viewModel.dismissAddTaskDialog()
                },
                onTaskCreate = { taskName, date, time, description ->
                    Log.d("TasksScreen", "Task Created: Name: $taskName, Date: $date, Time: $time, Desc: $description")
                    viewModel.dismissAddTaskDialog()
                }
            )
        }
    }
}


// VVV BAGIAN AddTaskDialog YANG SEHARUSNYA LENGKAP VVV
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismissRequest: () -> Unit,
    onTaskCreate: (String, String, String, String) -> Unit
) {
    var taskName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    var selectedDateText by rememberSaveable { mutableStateOf("Select Date") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        // Anda bisa mengatur initialSelectedDateMillis di sini jika perlu
        // initialSelectedDateMillis = System.currentTimeMillis()
    )

    var selectedTimeText by rememberSaveable { mutableStateOf("Select Time") }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        // Anda bisa mengatur initialHour dan initialMinute di sini
        // initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        // initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = true // Sesuaikan dengan format yang Anda inginkan
    )

    val configuration = LocalConfiguration.current
    val dialogWidth = (configuration.screenWidthDp * 0.9f).dp

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(dialogWidth)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                TopAppBar(
                    title = { Text("Add New Task", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Task Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(4.dp), // Bentuk standar OutlinedTextField
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = MaterialTheme.colorScheme.surface // Atau surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = "Select Date Icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = selectedDateText,
                                color = if (selectedDateText == "Select Date") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = "Select Time Icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = selectedTimeText,
                                color = if (selectedTimeText == "Select Time") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 150.dp),
                        maxLines = 7
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val finalDate = if (selectedDateText == "Select Date") "" else selectedDateText
                            val finalTime = if (selectedTimeText == "Select Time") "" else selectedTimeText
                            onTaskCreate(taskName, finalDate, finalTime, description)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Create New Task")
                    }
                }
            }
        }
    }

    // DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = millis
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Format tanggal standar
                        selectedDateText = sdf.format(calendar.time)
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePickerDialog (dibungkus Dialog generik karena TimePickerDialog M3 bisa sedikit berbeda cara pakainya)
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) { // Dialog pembungkus untuk TimePicker
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp) // Padding di sekitar Surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp), // Padding di dalam Surface
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select Time", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(20.dp))
                    TimePicker(state = timePickerState) // Komponen TimePicker M3
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End // Tombol OK dan Cancel ke kanan
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            showTimePicker = false
                            selectedTimeText = String.format(
                                Locale.getDefault(),
                                "%02d:%02d", // Format HH:mm
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}