package com.example.taskplannerapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskplannerapp.data.models.Task
import com.example.taskplannerapp.presentation.TaskViewModel
import com.example.taskplannerapp.data.models.Tag

@Composable
fun SearchScreen(
    viewModel: TaskViewModel,
    availableTags: List<Tag>
) {
    var query by remember { mutableStateOf("") }
    var searchOnlyUnfinished by remember { mutableStateOf(false) }

    val taskList by viewModel.tasks.collectAsState()
    val filtered = if (query.isBlank()) {
        emptyList()
    } else {
        taskList.filter { task ->
            val matchesQuery = task.title.contains(query, ignoreCase = true) ||
                    task.tags.any { it.contains(query, ignoreCase = true) }

            val matchesCompletion = !searchOnlyUnfinished || !task.isDone

            matchesQuery && matchesCompletion
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text("Поиск", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Поиск по названию или тэгу") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Искать только невыполненные")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = searchOnlyUnfinished,
                onCheckedChange = { searchOnlyUnfinished = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (filtered.isEmpty()) {
            Text("Ничего не найдено", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { task ->
                    TaskItem(
                        task = task,
                        availableTags = availableTags,
                        onToggle = {
                            viewModel.toggleDone(task)
                        },
                        onDelete = {
                            viewModel.deleteTask(task)
                        },
                        onEdit = {}
                    )
                }
            }
        }
    }
}
