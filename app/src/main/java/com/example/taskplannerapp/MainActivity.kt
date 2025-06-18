package com.example.taskplannerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import com.example.taskplannerapp.data.database.AppDatabase
import com.example.taskplannerapp.data.repository.TaskRepository
import com.example.taskplannerapp.presentation.TaskViewModel
import com.example.taskplannerapp.presentation.screens.TaskListScreen
import com.example.taskplannerapp.presentation.screens.AuthScreen
import com.example.taskplannerapp.presentation.screens.ProfileScreen
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import android.app.Activity
import androidx.compose.material.icons.filled.Search
import com.example.taskplannerapp.presentation.screens.SearchScreen
import com.example.taskplannerapp.presentation.screens.TagsScreen
import androidx.compose.material.icons.filled.Label
import com.example.taskplannerapp.presentation.TagsViewModel
import com.example.taskplannerapp.data.repository.TagsRepository
import com.example.taskplannerapp.ui.theme.TaskPlannerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(applicationContext)
        val taskRepository = TaskRepository(database.taskDao())
        val tagsRepository = TagsRepository()
        val taskViewModel = TaskViewModel(taskRepository)
        val tagsViewModel = TagsViewModel(tagsRepository)
        val availableTags = tagsViewModel.tags

        setContent {
            val availableTags = tagsViewModel.tags
            val view = LocalView.current
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = Color.Black.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
            TaskPlannerAppTheme(dynamicColor = false) {
                val auth = FirebaseAuth.getInstance()
                var isAuthenticated by remember { mutableStateOf(auth.currentUser != null) }

                if (!isAuthenticated) {
                    AuthScreen(onAuthSuccess = {
                        isAuthenticated = true
                        taskViewModel.syncTasks(this@MainActivity) // <--- вот это добавляем
                    })
                } else {
                    var selectedTab by remember { mutableStateOf(BottomNavItem.Tasks) }

                    Scaffold(
                        bottomBar = {
                            BottomNavigationBar(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                        }
                    ) { innerPadding ->
                        when (selectedTab) {
                            BottomNavItem.Tasks -> TaskListScreen(
                                viewModel = taskViewModel,
                                onLogout = { isAuthenticated = false }
                            )

                            BottomNavItem.Tags -> TagsScreen()

                            BottomNavItem.Search -> SearchScreen(
                                viewModel = taskViewModel,
                                availableTags = availableTags
                            )

                            BottomNavItem.Profile -> ProfileScreen(
                                onLogout = { isAuthenticated = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class BottomNavItem(val title: String, val icon: ImageVector) {
    Tasks("Задачи", Icons.Default.List),
    Tags("Тэги", Icons.Default.Label),
    Search("Поиск", Icons.Default.Search),
    Profile("Профиль", Icons.Default.Person)
}

@Composable
fun BottomNavigationBar(
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit
) {
    NavigationBar {
        BottomNavItem.values().forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selectedTab == item,
                onClick = { onTabSelected(item) }
            )
        }
    }
}
