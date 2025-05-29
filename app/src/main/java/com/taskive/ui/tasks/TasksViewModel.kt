package com.taskive.ui.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TasksViewModel : ViewModel() {

    private val _showAddTaskDialogFlow = MutableStateFlow(false)
    val showAddTaskDialogFlow: StateFlow<Boolean> = _showAddTaskDialogFlow.asStateFlow()

    init {
        Log.d("TasksViewModel", "ViewModel instance CREATED/RETAINED: $this")
    }

    fun openAddTaskDialog() {
        Log.d("TasksViewModel", "openAddTaskDialog called. Setting flow to true.")
        _showAddTaskDialogFlow.value = true
    }

    fun dismissAddTaskDialog() {
        Log.d("TasksViewModel", "dismissAddTaskDialog called. Setting flow to false.")
        _showAddTaskDialogFlow.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("TasksViewModel", "ViewModel CLEARED: $this")
    }
}