package com.taskive.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.taskive.model.Pet

class UserViewModel : ViewModel() {
    var username by mutableStateOf("User")
        private set

    private var _pets = mutableStateOf<List<Pet>>(emptyList())
    val pets: List<Pet> get() = _pets.value

    fun updateUsername(newUsername: String) {
        if (newUsername.isNotBlank()) {
            username = newUsername
        }
    }

    fun addPet(pet: Pet) {
        _pets.value = _pets.value + pet
    }

    fun updatePetStatus(petId: Int, newStatus: String) {
        _pets.value = _pets.value.map {
            if (it.id == petId) it.copy(status = newStatus)
            else it
        }
    }
}
