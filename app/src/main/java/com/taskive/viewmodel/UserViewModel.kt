package com.taskive.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val _username = mutableStateOf("John Doe")
    val username: State<String> = _username

    private val _level = mutableStateOf(5)
    val level: State<Int> = _level

    fun updateUsername(newName: String) {
        _username.value = newName
    }

    fun incrementLevel() {
        _level.value = _level.value + 1
    }
}
