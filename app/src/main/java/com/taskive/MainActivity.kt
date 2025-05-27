package com.taskive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType // <-- Import untuk NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument // <-- Import untuk navArgument
// import com.taskive.ui.addtask.AddTaskScreen // <-- Tidak digunakan lagi sebagai layar terpisah
import com.taskive.ui.dashboard.DashboardScreen
import com.taskive.ui.tasks.TasksScreen // <-- Import TasksScreen baru
import com.taskive.ui.theme.TaskiveTheme

val DarkPurple = Color(0xFF3A006A)
val MediumPurpleLight = Color(0xFF7B52AB)

// Screen.AddTask tetap ada untuk merepresentasikan tombol '+'
sealed class Screen(val route: String, val icon: ImageVector?, val label: String) {
    data object Dashboard : Screen("dashboard", Icons.Default.Home, "Home")
    data object Tasks : Screen("tasks", Icons.AutoMirrored.Filled.List, "Tasks")
    data object AddTask : Screen("addTask", Icons.Default.Add, "Add Task Action") // Label bisa diubah
    data object Store : Screen("store", Icons.Default.ShoppingCart, "Store")
    data object Profile : Screen("profile", Icons.Default.Person, "Profile")
}

// bottomNavItems tetap sama, AddTask adalah tombol aksi
val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Tasks,
    Screen.AddTask,
    Screen.Store,
    Screen.Profile,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskiveTheme {
                TaskiveApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskiveApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->
        AppNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) { DashboardScreen() }
        composable(
            route = "${Screen.Tasks.route}?showDialog={showDialog}", // Terima argumen
            arguments = listOf(navArgument("showDialog") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            val showDialog = backStackEntry.arguments?.getBoolean("showDialog") ?: false
            TasksScreen(navController = navController, showAddTaskPopupOnEntry = showDialog)
        }
        // composable(Screen.AddTask.route) { AddTaskScreen() } // <-- HAPUS INI
        composable(Screen.Store.route) { PlaceholderScreen(name = "Store") }
        composable(Screen.Profile.route) { PlaceholderScreen(name = "Profile") }
    }
}

@Composable
fun AppBottomBar(navController: NavHostController) {
    BottomAppBar(
        containerColor = DarkPurple,
        contentColor = Color.White.copy(alpha = 0.6f),
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                // Untuk isSelected, AddTask tidak akan pernah benar-benar "selected"
                // karena ia menavigasi ke TasksScreen
                val isSelected = currentDestination?.hierarchy?.any {
                    it.route == screen.route ||
                            // Jika tujuan saat ini adalah Tasks dan tombolnya adalah AddTask,
                            // anggap AddTask tidak selected.
                            (it.route?.startsWith(Screen.Tasks.route) == true && screen.route == Screen.AddTask.route)
                } == true && screen.route != Screen.AddTask.route


                val onClickAction: () -> Unit = {
                    if (screen == Screen.AddTask) {
                        // Navigasi ke TasksScreen dan minta dialog untuk muncul
                        navController.navigate("${Screen.Tasks.route}?showDialog=true") {
                            launchSingleTop = true // Hindari TasksScreen bertumpuk jika sudah terbuka
                        }
                    } else if (screen == Screen.Dashboard) {
                        if (currentDestination?.route != screen.route) {
                            navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        }
                    } else {
                        navController.navigate(screen.route) {
                            val startDestinationRoute = navController.graph.findStartDestination().route
                            if (startDestinationRoute != null) {
                                popUpTo(startDestinationRoute) {}
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                if (screen == Screen.AddTask) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MediumPurpleLight)
                            .clickable(onClick = onClickAction),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon!!,
                            contentDescription = screen.label,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(onClick = onClickAction)
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .wrapContentWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MediumPurpleLight else Color.Transparent)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.icon!!,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = screen.label,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ini Halaman $name",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}