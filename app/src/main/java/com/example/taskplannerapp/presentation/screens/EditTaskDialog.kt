package com.example.taskplannerapp.presentation.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.taskplannerapp.data.models.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (updatedText: String, updatedDeadline: Long?) -> Unit
) {
    val context = LocalContext.current
    var editedText by remember { mutableStateOf(task.title) }
    var editedDeadline by remember { mutableStateOf(task.deadline) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, 0, 0, 0)
            editedDeadline = calendar.timeInMillis
        },
        Calendar.getInstance().apply {
            timeInMillis = editedDeadline ?: System.currentTimeMillis()
        }.get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать задачу") },
        text = {
            Column {
                TextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    label = { Text("Текст задачи") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { datePickerDialog.show() }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Дата"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Выбрать дату")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = editedDeadline?.let {
                        "Дедлайн: " + SimpleDateFormat("d MMMM yyyy", Locale("ru"))
                            .format(Date(it))
                    } ?: "Дедлайн: без даты",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(editedText, editedDeadline)
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
