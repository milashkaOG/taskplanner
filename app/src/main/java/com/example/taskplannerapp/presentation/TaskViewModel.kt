package com.example.taskplannerapp.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskplannerapp.data.models.Task
import com.example.taskplannerapp.data.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.taskplannerapp.data.models.Tag
import androidx.compose.runtime.mutableStateOf



class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val tasks = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _availableTags = mutableStateListOf<Tag>()
    val availableTags: List<Tag> get() = _availableTags

    var isSyncing = mutableStateOf(false)
        private set

    fun syncTasks(context: Context) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val ref = FirebaseDatabase
                .getInstance("https://taskmanager-8f753-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users/$uid/tasks")

            val snapshot = ref.get().await()
            val tasks = snapshot.children.mapNotNull { it.getValue(Task::class.java) }

            repository.insertAll(tasks)
        }
    }

    fun loadAvailableTags(context: Context) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            val ref = FirebaseDatabase
                .getInstance("https://taskmanager-8f753-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users/$uid/tags")

            val snapshot = ref.get().await()
            _availableTags.clear()
            _availableTags.addAll(
                snapshot.children.mapNotNull {
                    val name = it.child("name").getValue(String::class.java)
                    val color = it.child("color").getValue(String::class.java)
                    if (name != null && color != null) Tag(name, color) else null
                }
            )
        }
    }

    fun addTask(title: String, deadline: Long?, context: Context, tags: List<String>) {
        val task = Task(title = title, deadline = deadline, tags = tags)
        viewModelScope.launch {
            repository.addTask(task, context)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleDone(task: Task) {
        val updated = task.copy(isDone = !task.isDone)
        viewModelScope.launch {
            repository.updateTask(updated)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }
}

