package com.example.taskplannerapp.data.repository

import android.content.Context
import android.widget.Toast
import com.example.taskplannerapp.data.database.TaskDao
import com.example.taskplannerapp.data.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.*

class TaskRepository(private val dao: TaskDao) {

    /**
     * Поток задач только текущего пользователя
     */
    val allTasks: Flow<List<Task>>
        get() {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            return if (uid != null) {
                dao.getAllByUser(uid)
            } else {
                flowOf(emptyList())
            }
        }

    /**
     * Добавить задачу (локально и в Firebase)
     */
    suspend fun syncFromFirebase(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firebasePath = FirebaseDatabase
            .getInstance("https://taskmanager-8f753-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("users/$uid/tasks")

        try {
            val snapshot = firebasePath.get().await()
            val tasks = snapshot.children.mapNotNull { it.getValue(Task::class.java) }

            // Обновим локальную базу — можно сначала очистить задачи пользователя
            dao.clearTasksForUser(uid) // добавь этот метод в Dao
            dao.insertAll(tasks)

            Toast.makeText(context, "Синхронизация завершена", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка синхронизации: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun addTask(task: Task, context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }

        val firebaseTaskId = task.firebaseId ?: UUID.randomUUID().toString()
        val taskToSave = task.copy(firebaseId = firebaseTaskId, userId = uid)

        dao.insert(taskToSave)

        val firebasePath = FirebaseDatabase
            .getInstance("https://taskmanager-8f753-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("users/$uid/tasks/$firebaseTaskId")

        try {
            firebasePath.setValue(taskToSave).await()
            Toast.makeText(context, "Задача добавлена", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка при сохранении в Firebase: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Удалить задачу (локально и из Firebase)
     */
    suspend fun deleteTask(task: Task) {
        dao.delete(task)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firebaseId = task.firebaseId ?: return

        FirebaseDatabase
            .getInstance("https://taskmanager-8f753-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("users/$uid/tasks/$firebaseId")
            .removeValue()
    }

    /**
     * Обновить задачу (локально и в Firebase)
     */
    suspend fun updateTask(task: Task) {
        dao.update(task)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firebaseId = task.firebaseId ?: return

        FirebaseDatabase
            .getInstance("https://taskmanager-8f753-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("users/$uid/tasks/$firebaseId")
            .setValue(task)
    }

    suspend fun insertAll(tasks: List<Task>) {
        dao.insertAll(tasks)
    }
}
