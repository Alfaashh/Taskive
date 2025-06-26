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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.taskive.navigation.Screen
import com.taskive.ui.theme.*
import com.taskive.ui.viewmodel.TaskViewModel
import com.taskive.ui.viewmodel.StoreViewModel
import com.taskive.ui.viewmodel.UserViewModel
import com.taskive.ui.tasks.EditTaskDialog
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    taskViewModel: TaskViewModel,
    userViewModel: UserViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPurpleBackground)
            .padding(horizontal = 20.dp)
    ) {
        item { TopSection(userViewModel) }
        item { Spacer(modifier = Modifier.height(30.dp)) }
        item { GreetingSection(userViewModel.username) }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { SummarySection(taskViewModel) }
        item { Spacer(modifier = Modifier.height(30.dp)) }
        item { RecentTasksSection(navController, taskViewModel) }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun TopSection(userViewModel: UserViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        val taskiveText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = MediumPurpleDark)) {
                append("TASK")
            }
            withStyle(style = SpanStyle(color = MediumPurpleLight)) {
                append("IVE")
            }
        }

        Text(
            text = taskiveText,
            style = MaterialTheme.typography.displayLarge
        )

        // Add coin display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFFFFC107), shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = userViewModel.coins.toString(),
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun GreetingSection(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "Hi, $username",
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = TextColorDark
        )
        Text(
            text = "Monday, 30 May 2025",
            style = MaterialTheme.typography.bodyLarge,
            color = TextColorLight,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
private fun SummarySection(taskViewModel: TaskViewModel) {
    val currentTime = System.currentTimeMillis()

    val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val endOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    val startOfNextDay = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Count tasks based on categories
    val upcomingTasks = taskViewModel.tasks.count { task ->
        !task.isCompleted && task.deadline != null && task.deadline > endOfDay
    }

    val todayTasks = taskViewModel.tasks.count { task ->
        !task.isCompleted && task.deadline != null &&
        task.deadline in startOfDay..endOfDay
    }

    val overdueTasks = taskViewModel.tasks.count { task ->
        !task.isCompleted && task.deadline != null && task.deadline < startOfDay
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryCard(count = upcomingTasks.toString(), label = "Upcoming")
        SummaryCard(count = todayTasks.toString(), label = "Today")
        SummaryCard(count = overdueTasks.toString(), label = "Overdue")
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
                color = TextColorDark
            )
            Text(
                text = "See all",
                fontFamily = Nunito,
                fontSize = 14.sp,
                color = MediumPurpleDark,
                modifier = Modifier.clickable {
                    taskViewModel.dismissEditTaskDialog()
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        val recentTasks = taskViewModel.recentTasks

        if (recentTasks.isEmpty()) {
            Text(
                text = "No tasks yet",
                fontFamily = Nunito,
                color = TextColorLight,
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
