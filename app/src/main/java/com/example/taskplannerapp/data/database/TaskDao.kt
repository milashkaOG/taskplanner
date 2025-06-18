package com.example.taskplannerapp.data.database

import androidx.room.*
import com.example.taskplannerapp.data.models.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :uid")
    fun getAllByUser(uid: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("DELETE FROM tasks WHERE userId = :uid")
    suspend fun clearTasksForUser(uid: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)
}
