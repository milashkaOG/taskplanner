package com.example.taskplannerapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.taskplannerapp.data.models.Task
import com.example.taskplannerapp.data.models.Tag
import androidx.compose.ui.text.style.TextDecoration


@Composable
fun TaskItem(
    task: Task,
    availableTags: List<Tag>,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isDone) Color(0xFFEEEEEE) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = { onToggle() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                        color = if (task.isDone) Color.Gray else Color.Unspecified
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (!task.isDone) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = if (task.isDone) Color.Gray else Color.Unspecified
                    )
                }
            }

            if (task.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    task.tags.forEach { tagName ->
                        val tag = availableTags.find { it.name == tagName }
                        val tagColor = namedColors[tag?.color ?: "gray"] ?: Color.Gray

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tagColor
                        ) {
                            Text(
                                text = tagName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
