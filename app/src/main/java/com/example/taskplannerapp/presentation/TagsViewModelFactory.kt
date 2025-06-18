package com.example.taskplannerapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskplannerapp.data.repository.TagsRepository

class TagsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TagsViewModel(TagsRepository()) as T
    }
}
