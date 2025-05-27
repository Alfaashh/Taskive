package com.taskive.ui.tasks

import android.os.Build
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
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.android.gms.tasks.Tasks
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    showAddTaskPopupOnEntry: Boolean
) {
    val showDialog = rememberSaveable(showAddTaskPopupOnEntry) { mutableStateOf(showAddTaskPopupOnEntry) }

    LaunchedEffect(showAddTaskPopupOnEntry, navController.currentBackStackEntry) {
        // Tampilkan dialog jika argumen true DAN kita benar-benar berada di TasksScreen
        // Ini untuk mencegah dialog muncul jika kita bernavigasi *dari* TasksScreen
        // ke tempat lain lalu kembali dengan back button (kecuali jika argumennya masih true)
        if (showAddTaskPopupOnEntry && navController.currentDestination?.route?.startsWith(Screen.tasks.route) == true) {
            showDialog.value = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .then(
                    if (showDialog.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(radius = 10.dp, edgeTreatment = ShaderBrush.Clamp)
                    } else if (showDialog.value) {
                        Modifier.background(Color.Black.copy(alpha = 0.3f)) // Scrim untuk API < 31
                    } else {
                        Modifier
                    }
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Daftar Tugas Akan Tampil di Sini", style = MaterialTheme.typography.headlineMedium)
            // TODO: Implementasi LazyColumn atau UI daftar tugas Anda
        }

        if (showDialog.value) {
            AddTaskDialog(
                onDismissRequest = { showDialog.value = false },
                onTaskCreate = { taskName, category, date, time, description ->
                    // TODO: Logika untuk menyimpan task baru
                    println("Task Created: $taskName, Cat: $category, Date: $date, Time: $time, Desc: $description")
                    showDialog.value = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismissRequest: () -> Unit,
    onTaskCreate: (String, String, String, String, String) -> Unit
) {
    var taskName by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    var selectedDateText by rememberSaveable { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState() // Tidak perlu initialSelectedDateMillis jika defaultnya hari ini

    var selectedTimeText by rememberSaveable { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(is24Hour = true) // Sesuaikan is24Hour jika perlu

    val configuration = LocalConfiguration.current
    val dialogWidth = (configuration.screenWidthDp * 0.9f).dp

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface, // Gunakan warna dari tema
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
                        containerColor = Color.Transparent, // Transparan agar menyatu dengan Surface dialog
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Task Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                        // Anda bisa menggantinya dengan ExposedDropdownMenuBox nanti
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = selectedDateText,
                        onValueChange = { /* Tidak diubah langsung */ },
                        label = { Text("Date") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { showDatePicker = true }),
                        readOnly = true,
                        enabled = false, // Tetap false, warna diatur di colors
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = "Select Date")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors( // Warna agar terlihat seperti bisa diisi
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = selectedTimeText,
                        onValueChange = { /* Tidak diubah langsung */ },
                        label = { Text("Time") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { showTimePicker = true }),
                        readOnly = true,
                        enabled = false, // Tetap false
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Filled.Schedule, contentDescription = "Select Time")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors( // Warna agar terlihat seperti bisa diisi
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp),
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onTaskCreate(taskName, category, selectedDateText, selectedTimeText, description)
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
                    // Konversi epoch millis ke format tanggal yang diinginkan
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = millis
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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

    // TimePickerDialog (dibungkus Dialog generik)
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp), // Sesuaikan bentuk jika perlu
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp, // Sesuaikan elevasi jika perlu
                modifier = Modifier.padding(16.dp) // Padding untuk dialog time picker
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select Time", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(20.dp)) // Jarak lebih besar
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(20.dp)) // Jarak lebih besar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End // Tombol ke kanan
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            showTimePicker = false
                            selectedTimeText = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
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