package com.example.taskplannerapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskplannerapp.data.models.Tag
import com.example.taskplannerapp.presentation.TagsViewModel
import com.example.taskplannerapp.presentation.TagsViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope

val namedColors = mapOf(
    "red" to Color(0xFFE57373),
    "green" to Color(0xFF81C784),
    "blue" to Color(0xFF64B5F6),
    "yellow" to Color(0xFFFFF176),
    "purple" to Color(0xFFBA68C8),
    "cyan" to Color(0xFF4DD0E1),
    "gray" to Color(0xFFB0BEC5),
    "orange" to Color(0xFFFFB74D)
)

@Composable
fun TagsScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewModel: TagsViewModel = viewModel(factory = TagsViewModelFactory())
    val tags by remember { derivedStateOf { viewModel.tags } }

    var showDialog by remember { mutableStateOf(false) }
    var tagName by remember { mutableStateOf("") }
    var selectedColorName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadTags(context)
    }

    Scaffold(
        floatingActionButton = {
            if (tags.size < 8) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(bottom = 100.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить тэг")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(24.dp)
        ) {
            Text("Тэги", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(32.dp))

            if (tags.isEmpty()) {
                Text("Тэгов пока нет")
            } else {
                tags.forEachIndexed { index, tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Text("${index + 1}.", modifier = Modifier.width(24.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(namedColors[tag.color] ?: Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tag.name, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.deleteTag(tag, context) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить тег",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }

        if (showDialog) {
            val usedColors = tags.map { it.color }
            val usedNames = tags.map { it.name.lowercase() }
            val isDuplicateName = tagName.trim().lowercase() in usedNames
            val isFormValid = tagName.isNotBlank() && !isDuplicateName && selectedColorName != null && selectedColorName !in usedColors


            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {},
                title = { Text("Новый тэг") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = tagName,
                            onValueChange = { if (it.length <= 10) tagName = it },
                            label = { Text("Название") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = isDuplicateName,
                            supportingText = {
                                if (isDuplicateName) Text("Тег с таким названием уже существует")
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Цвет:")
                        Row {
                            namedColors.forEach { (name, color) ->
                                val isUsed = name in usedColors
                                val displayColor = if (isUsed) color.copy(alpha = 0.2f) else color

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(displayColor)
                                        .border(
                                            width = if (selectedColorName == name) 2.dp else 1.dp,
                                            color = if (selectedColorName == name) Color.Black else Color.Gray,
                                            shape = CircleShape
                                        )
                                        .clickable(enabled = !isUsed) {
                                            selectedColorName = name
                                        }
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val tag = Tag(name = tagName.trim(), color = selectedColorName!!)
                                viewModel.addTag(tagName.trim(), selectedColorName!!, context)
                                tagName = ""
                                selectedColorName = null
                                showDialog = false
                            },
                            enabled = isFormValid,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Добавить")
                        }
                    }
                }
            )
        }
    }
}
