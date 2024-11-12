package com.fisiaewiso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RiddleViewModelFactory(private val repository: ResultRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RiddleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RiddleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}