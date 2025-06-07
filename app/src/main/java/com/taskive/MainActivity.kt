package com.taskive

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.taskive.ui.dashboard.DashboardScreen
import com.taskive.ui.store.StoreScreen
import com.taskive.ui.tasks.TasksScreen
import com.taskive.ui.theme.TaskiveTheme
import com.taskive.ui.viewmodel.StoreViewModel
import com.taskive.ui.viewmodel.TaskViewModel
import com.taskive.ui.viewmodel.UserViewModel
import com.taskive.ui.profile.ProfileScreen

// Definisikan warna global
val DarkPurple = Color(0xFF3A006A)
val MediumPurpleLight = Color(0xFF7B52AB) // Mungkin tidak dipakai lagi di sini

// Konstanta untuk argumen navigasi (jika ada layar lain yang butuh, jika tidak, bisa lokal)
// Untuk sekarang TasksScreen tidak lagi mengambil argumen ini dari MainActivity
// const val NAV_ARG_SHOW_DIALOG = "showDialog"

// Hanya 4 Screen utama untuk BottomAppBar
sealed class Screen(val route: String, val icon: ImageVector, val label: String) { // Icon tidak lagi nullable
    data object Dashboard : Screen("dashboard", Icons.Default.Home, "Home")
    data object Tasks : Screen("tasks", Icons.AutoMirrored.Filled.List, "Tasks")
    // AddTask dihapus dari sini
    data object Store : Screen("store", Icons.Default.ShoppingCart, "Store")
    data object Profile : Screen("profile", Icons.Default.Person, "Profile")
}

// Hanya 4 item untuk BottomAppBar
val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Tasks,
    Screen.Store,
    Screen.Profile,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskiveTheme {
                TaskiveApp(application)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskiveApp(application: Application) {
    val navController = rememberNavController()
    val storeViewModel: StoreViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StoreViewModel(application) as T
            }
        }
    )
    val taskViewModel: TaskViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskViewModel(application, storeViewModel) as T
            }
        }
    )
    val userViewModel: UserViewModel = viewModel()

    Scaffold(
        bottomBar = { TaskiveBottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    navController = navController,
                    taskViewModel = taskViewModel,
                    storeViewModel = storeViewModel,
                    userViewModel = userViewModel
                )
            }
            composable(Screen.Tasks.route) {
                TasksScreen(taskViewModel = taskViewModel)
            }
            composable(Screen.Store.route) {
                StoreScreen(
                    navController = navController,
                    storeViewModel = storeViewModel,
                    userViewModel = userViewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(userViewModel = userViewModel)
            }
        }
    }
}

@Composable
fun TaskiveBottomBar(navController: NavHostController) {
    BottomAppBar(
        containerColor = DarkPurple,
        contentColor = Color.White.copy(alpha = 0.6f),
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        Log.d("AppBottomBar", "Current Destination: ${currentDestination?.route}")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen -> // Sekarang iterasi 4 item
                val isSelected = currentDestination?.hierarchy?.any { navDest ->
                    navDest.route?.startsWith(screen.route) == true
                } == true

                IconButton(
                    onClick = {
                        Log.d("AppBottomBar", "Clicked: ${screen.label}, Current: ${currentDestination?.route}, Target: ${screen.route}")
                        if (screen == Screen.Dashboard) {
                            if (currentDestination?.route != screen.route) {
                                navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                            }
                        } else {
                            val startDestinationRoute = navController.graph.findStartDestination().route
                            if (startDestinationRoute != null) {
                                navController.navigate(screen.route) {
                                    popUpTo(startDestinationRoute) {
                                        saveState = false // Untuk stabilitas
                                    }
                                    launchSingleTop = true
                                    restoreState = false // Untuk stabilitas
                                }
                            } else {
                                navController.navigate(screen.route) { launchSingleTop = true }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight() // Bagi rata dan isi tinggi
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box( // Box indikator
                            modifier = Modifier
                                .height(32.dp)
                                .wrapContentWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MediumPurpleLight else Color.Transparent)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.icon, // Icon tidak lagi nullable
                                contentDescription = screen.label, // Content desc dari label
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
