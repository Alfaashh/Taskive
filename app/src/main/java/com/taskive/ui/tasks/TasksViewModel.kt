package com.taskive.ui.tasks

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskive.NAV_ARG_SHOW_DIALOG // <-- IMPORT DARI MAINACTIVITY.KT (paket com.taskive)
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class TasksViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _showAddTaskDialogFlow = MutableStateFlow(false)
    val showAddTaskDialogFlow: StateFlow<Boolean> = _showAddTaskDialogFlow.asStateFlow()

    init {
        Log.d("TasksViewModel", "ViewModel initialized. Instance: $this")
        // Amati perubahan pada argumen navigasi 'showDialog'
        // NAV_ARG_SHOW_DIALOG diimpor dari paket com.taskive (MainActivity.kt)
        savedStateHandle.getStateFlow(NAV_ARG_SHOW_DIALOG, false)
            .onEach { shouldShowFromNav ->
                Log.d("TasksViewModel", "Nav arg '$NAV_ARG_SHOW_DIALOG' in SavedStateHandle for $this changed to: $shouldShowFromNav")
                if (shouldShowFromNav) {
                    Log.d("TasksViewModel", "Setting _showAddTaskDialogFlow to true because nav arg is true. Instance: $this")
                    _showAddTaskDialogFlow.value = true
                    // "Konsumsi" argumen agar tidak memicu dialog lagi secara otomatis
                    // sampai ada navigasi BARU dengan argumen true.
                    Log.d("TasksViewModel", "Consuming nav arg by setting it to false in SavedStateHandle. Instance: $this")
                    savedStateHandle[NAV_ARG_SHOW_DIALOG] = false
                }
            }
            .launchIn(viewModelScope)
    }

    fun dismissAddTaskDialog() {
        Log.d("TasksViewModel", "dismissAddTaskDialog called. Hiding dialog. Instance: $this")
        _showAddTaskDialogFlow.value = false
        // Saat dialog ditutup, argumen di SavedStateHandle sudah seharusnya false karena "dikonsumsi" di init.
        // savedStateHandle[NAV_ARG_SHOW_DIALOG] = false; // Baris ini mungkin tidak perlu lagi di sini.
    }

    fun requestShowAddTaskDialogExplicitly() {
        Log.d("TasksViewModel", "requestShowAddTaskDialogExplicitly called. Instance: $this")
        _showAddTaskDialogFlow.value = true
        // Jika ini dipanggil, kita juga "konsumsi" argumen navigasi jika ada,
        // agar tidak ada pemicu ganda.
        savedStateHandle[NAV_ARG_SHOW_DIALOG] = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("TasksViewModel", "ViewModel CLEARED: $this")
    }
}