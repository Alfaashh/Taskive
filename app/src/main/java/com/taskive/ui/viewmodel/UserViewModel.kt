package com.taskive.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.taskive.model.Pet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("taskive_user", Context.MODE_PRIVATE)
    private val gson = Gson()

    var username by mutableStateOf(loadUsername())
        private set

    private var _pets = mutableStateOf<List<Pet>>(loadPets())
    val pets: List<Pet> get() = _pets.value

    private fun loadUsername(): String {
        return sharedPreferences.getString("username", "User") ?: "User"
    }

    private fun loadPets(): List<Pet> {
        val petsJson = sharedPreferences.getString("pets", "[]")
        val type = object : TypeToken<List<Pet>>() {}.type
        return gson.fromJson(petsJson, type) ?: emptyList()
    }

    fun updateUsername(newUsername: String) {
        if (newUsername.isNotBlank()) {
            username = newUsername
            sharedPreferences.edit().putString("username", newUsername).apply()
        }
    }

    fun addPet(pet: Pet) {
        _pets.value = _pets.value + pet
        savePets()
    }

    fun updatePetStatus(petId: Int, newStatus: String) {
        _pets.value = _pets.value.map {
            if (it.id == petId) it.copy(status = newStatus)
            else it
        }
        savePets()
    }

    private fun savePets() {
        val petsJson = gson.toJson(_pets.value)
        sharedPreferences.edit().putString("pets", petsJson).apply()
    }
}
