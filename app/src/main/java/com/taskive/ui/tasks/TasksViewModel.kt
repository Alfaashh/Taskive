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

    fun userWantsToShowDialog() {
        Log.d("TasksViewModel", "userWantsToShowDialog: Setting _showAddTaskDialogFlow to true. Instance: $this")
        _showAddTaskDialogFlow.value = true
    }

    fun userWantsToDismissDialog() {
        Log.d("TasksViewModel", "userWantsToDismissDialog: Setting _showAddTaskDialogFlow to false. Instance: $this")
        _showAddTaskDialogFlow.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("TasksViewModel", "ViewModel CLEARED: $this")
    }
}