package com.example.taskplannerapp.data.repository

import android.content.Context
import android.widget.Toast
import com.example.taskplannerapp.data.models.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.*

class TagsRepository {

    suspend fun addTag(tag: Tag, context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "UID = null", Toast.LENGTH_SHORT).show()
            return
        }

        val firebaseTagId = tag.name // ключ в Firebase — название тэга
        val tagToSave = tag.copy(firebaseId = firebaseTagId)

        val firebasePath = FirebaseDatabase
            .getInstance("https://taskmanager-8f753-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("users/$uid/tags/$firebaseTagId")

        try {
            firebasePath.setValue(tagToSave).await()
            Toast.makeText(context, "Тег создан", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun deleteTag(tag: Tag, context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firebaseId = tag.firebaseId ?: return

        val firebasePath = FirebaseDatabase
            .getInstance("https://taskmanager-8f753-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("users/$uid/tags/$firebaseId")

        try {
            firebasePath.removeValue().await()
            Toast.makeText(context, "Тег удалён", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка при удалении: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun loadTags(context: Context): List<Tag> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()

        val ref = FirebaseDatabase
            .getInstance("https://taskmanager-8f753-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("users/$uid/tags")

        return try {
            val snapshot = ref.get().await()
            snapshot.children.mapNotNull { snap ->
                val tag = snap.getValue(Tag::class.java)
                tag?.copy(firebaseId = snap.key)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка загрузки тегов: ${e.message}", Toast.LENGTH_LONG).show()
            emptyList()
        }
    }
}
