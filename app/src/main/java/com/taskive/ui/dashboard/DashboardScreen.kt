package com.taskive.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.taskive.navigation.Screen
import com.taskive.ui.theme.Nunito
import com.taskive.ui.theme.MediumPurpleDark
import com.taskive.ui.theme.MediumPurpleLight
import com.taskive.ui.viewmodel.TaskViewModel
import com.taskive.ui.viewmodel.StoreViewModel
import com.taskive.ui.tasks.EditTaskDialog

val LightPurpleBackground = Color(0xFFF8F6FF)
val TextColorDarkGlobal = Color(0xFF333333)
val TextColorLightGlobal = Color.Gray

@Composable
fun DashboardScreen(
    navController: NavHostController = rememberNavController(),
    taskViewModel: TaskViewModel,
    storeViewModel: StoreViewModel = viewModel()
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPurpleBackground)
            .padding(horizontal = 20.dp)
    ) {
        item { TopSection(storeViewModel) }
        item { Spacer(modifier = Modifier.height(30.dp)) }
        item { GreetingSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { SummarySection(taskViewModel) }
        item { Spacer(modifier = Modifier.height(30.dp)) }
        item { RecentTasksSection(navController, taskViewModel) }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun TopSection(storeViewModel: StoreViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        val taskiveText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MediumPurpleDark
                )
            ) {
                append("TASK")
            }
            withStyle(
                style = SpanStyle(
                    color = MediumPurpleLight
                )
            ) {
                append("IVE")
            }
        }

        Text(
            text = taskiveText,
            style = MaterialTheme.typography.displayLarge
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFFFFC107), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${storeViewModel.coins.value}",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextColorDarkGlobal
            )
        }
    }
}

@Composable
private fun GreetingSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "Hi, Hitam",
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = TextColorDarkGlobal
        )
        Text(
            text = "Monday, 30 May 2025",
            style = MaterialTheme.typography.bodyLarge,
            color = TextColorLightGlobal,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
private fun SummarySection(taskViewModel: TaskViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryCard(count = "0", label = "Upcoming")
        SummaryCard(count = "0", label = "Today")
        SummaryCard(count = taskViewModel.completedCount.value.toString(), label = "Completed")
    }
}

@Composable
private fun SummaryCard(count: String, label: String) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MediumPurpleLight, MediumPurpleDark)
    )

    Card(
        modifier = Modifier.size(width = 110.dp, height = 120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(count, fontSize = 32.sp, fontFamily = Nunito, fontWeight = FontWeight.Bold, color = Color.White)
                Text("tasks", fontSize = 14.sp, fontFamily = Nunito, color = Color.White.copy(alpha = 0.8f))
                Text(label, fontSize = 16.sp, fontFamily = Nunito, fontWeight = FontWeight.Normal, color = Color.White)
            }
        }
    }
}

@Composable
private fun RecentTasksSection(
    navController: NavHostController,
    taskViewModel: TaskViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Tasks",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextColorDarkGlobal
            )
            Text(
                text = "See all",
                fontFamily = Nunito,
                fontSize = 14.sp,
                color = MediumPurpleDark,
                modifier = Modifier.clickable {
                    try {
                        taskViewModel.dismissEditTaskDialog()
                        navController.navigate(Screen.Tasks.route)
                    } catch (e: Exception) {
                        // Navigation failed, stay on current screen
                    }
                }
            )
        }

        val recentTasks = taskViewModel.recentTasks

        if (recentTasks.isEmpty()) {
            Text(
                text = "No tasks yet",
                fontFamily = Nunito,
                color = TextColorLightGlobal,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val gradientBrush = Brush.verticalGradient(
                    colors = listOf(MediumPurpleLight, MediumPurpleDark)
                )

                recentTasks.forEach { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // First select the task
                                taskViewModel.selectTask(task)
                                // Then open the edit dialog
                                taskViewModel.openEditTaskDialog()
                            },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(gradientBrush)
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = Nunito
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.datetime,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontFamily = Nunito
                                )
                                Text(
                                    text = task.daysLeft,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Show edit dialog if a task is selected
        if (taskViewModel.showEditTaskDialog.value && taskViewModel.selectedTask.value != null) {
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
