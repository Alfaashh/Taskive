package com.taskive.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Tasks : Screen("tasks")
    object Store : Screen("store")

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                Dashboard.route -> Dashboard
                Tasks.route -> Tasks
                Store.route -> Store
                else -> Dashboard
            }
        }
    }
}
