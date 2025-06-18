package com.example.taskplannerapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taskplannerapp.data.utils.Converters

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isDone: Boolean = false,
    val firebaseId: String? = null,
    val deadline: Long? = null,
    val tags: List<String> = emptyList(),
    val userId: String = ""
)

