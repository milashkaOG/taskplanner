package com.example.taskplannerapp.presentation.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.taskplannerapp.data.models.Task
import com.example.taskplannerapp.presentation.TaskViewModel
import java.util.*
import androidx.compose.ui.input.pointer.pointerInput
import java.text.SimpleDateFormat
import com.example.taskplannerapp.presentation.screens.TaskItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.foundation.layout.FlowRow






fun normalizeToMidnight(timestamp: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskListScreen(viewModel: TaskViewModel, onLogout: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val sectionExpanded = remember { mutableStateMapOf<Long, Boolean>() }
    val taskList by viewModel.tasks.collectAsState()
    val context = LocalContext.current
    var showFilterDialog by remember { mutableStateOf(false) }
    var hideCompleted by remember { mutableStateOf(false) }
    val filterSelectedTags = remember { mutableStateListOf<String>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskText by remember { mutableStateOf("") }
    var selectedDeadline by remember { mutableStateOf<Long?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    val allTags = viewModel.availableTags
    val selectedTags = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        viewModel.loadAvailableTags(context)
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, 0, 0, 0)
            selectedDeadline = calendar.timeInMillis
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(bottom = 100.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ваши задачи", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))

                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтр")
                    }
                }


                Spacer(modifier = Modifier.height(32.dp))

                val filteredTasks = taskList.filter { task ->
                    (!hideCompleted || !task.isDone) &&
                            (filterSelectedTags.isEmpty() || task.tags.any { it in filterSelectedTags })
                }

                val groupedTasks = remember(filteredTasks) {
                    filteredTasks.groupBy { task ->
                        task.deadline?.let {
                            val cal = Calendar.getInstance().apply { timeInMillis = it }
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            cal.timeInMillis
                        } ?: -1L
                    }.toSortedMap(compareBy { if (it == -1L) Long.MIN_VALUE else it })
                }

                LazyColumn {
                    if (taskList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Задач нет", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    } else {
                        groupedTasks.forEach { (deadline, tasksForDate) ->
                            val expanded = sectionExpanded[deadline] ?: true
                            val total = tasksForDate.size
                            val done = tasksForDate.count { it.isDone }

                            item {
                                val formattedDate = if (deadline == -1L) {
                                    "Без дедлайна"
                                } else {
                                    val format = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
                                    format.format(Date(deadline))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    val overdue = tasksForDate.count {
                                        !it.isDone && it.deadline != null && it.deadline < System.currentTimeMillis()
                                    }

                                    if (overdue > 0) {
                                        Text(
                                            text = "Просрочено: $overdue",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "$done/$total",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }

                                    Text(
                                        text = if (expanded) "▼" else "▶",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.clickable {
                                            sectionExpanded[deadline] = !expanded
                                        }
                                    )
                                }
                            }

                            if (expanded) {
                                val sortedTasks = tasksForDate.sortedBy { it.isDone }
                                items(sortedTasks) { task ->
                                    TaskItem(
                                        task = task,
                                        availableTags = allTags,
                                        onToggle = { viewModel.toggleDone(task) },
                                        onDelete = { viewModel.deleteTask(task) },
                                        onEdit = { taskToEdit = task }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    confirmButton = {},
                    title = { Text("Новая задача") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newTaskText,
                                onValueChange = { newTaskText = it },
                                label = { Text("Название задачи") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Теги:")
                            FlowRow(
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                allTags.forEach { tag ->
                                    val tagColor = namedColors[tag.color] ?: Color.Gray
                                    val isSelected = selectedTags.contains(tag.name)

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isSelected) tagColor else tagColor.copy(alpha = 0.2f))
                                            .clickable {
                                                if (isSelected) {
                                                    selectedTags.remove(tag.name)
                                                } else {
                                                    selectedTags.add(tag.name)
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = tag.name,
                                            color = if (isSelected) Color.White else Color.Black,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }



                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { datePickerDialog.show() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Дата",
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Выбрать дату")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (newTaskText.isNotBlank()) {
                                        val normalizedDeadline = selectedDeadline?.let { normalizeToMidnight(it) }
                                        viewModel.addTask(
                                            newTaskText,
                                            normalizedDeadline,
                                            context,
                                            selectedTags.toList()
                                        )
                                        newTaskText = ""
                                        selectedDeadline = null
                                        selectedTags.clear()
                                        showAddDialog = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Добавить")
                            }
                        }
                    }
                )
            }

            taskToEdit?.let { task ->
                EditTaskDialog(
                    task = task,
                    onDismiss = { taskToEdit = null },
                    onSave = { updatedText, updatedDeadline ->
                        val updatedTask = task.copy(title = updatedText, deadline = updatedDeadline)
                        viewModel.updateTask(updatedTask)
                        taskToEdit = null
                    }
                )
            }

            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    confirmButton = {},
                    title = { Text("Фильтры") },
                    text = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = hideCompleted,
                                    onCheckedChange = { hideCompleted = it }
                                )
                                Text("Скрыть выполненные задачи")
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Фильтр по тегам:")

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                allTags.forEach { tag ->
                                    val isSelected = filterSelectedTags.contains(tag.name)
                                    val tagColor = namedColors[tag.color] ?: Color.Gray

                                    Surface(
                                        color = if (isSelected) tagColor else Color.Transparent,
                                        border = BorderStroke(1.dp, tagColor),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .clickable {
                                                if (isSelected) filterSelectedTags.remove(tag.name)
                                                else filterSelectedTags.add(tag.name)
                                            }
                                    ) {
                                        Text(
                                            text = tag.name,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            color = if (isSelected) Color.White else Color.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { showFilterDialog = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Применить")
                                }

                                OutlinedButton(
                                    onClick = {
                                        hideCompleted = false
                                        filterSelectedTags.clear()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Сбросить")
                                }
                            }
                        }
                    }
                )
            }

        }
    }
}
