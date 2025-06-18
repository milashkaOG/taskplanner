package com.example.taskplannerapp.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskplannerapp.data.models.Tag
import com.example.taskplannerapp.data.repository.TagsRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf


class TagsViewModel(private val repository: TagsRepository) : ViewModel() {
    private val _tags = mutableStateListOf<Tag>()
    val tags: List<Tag> get() = _tags

    fun loadTags(context: Context) {
        viewModelScope.launch {
            _tags.clear()
            _tags.addAll(repository.loadTags(context))
        }
    }

    fun addTag(name: String, color: String, context: Context) {
        val tag = Tag(name = name, color = color, firebaseId = name)
        viewModelScope.launch {
            repository.addTag(tag, context)
            _tags.add(tag)
        }
    }

    fun deleteTag(tag: Tag, context: Context) {
        viewModelScope.launch {
            repository.deleteTag(tag, context)
            _tags.remove(tag)
        }
    }
}

