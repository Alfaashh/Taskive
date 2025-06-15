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

    var currentLevel by mutableStateOf(loadLevel())
        private set

    var currentXP by mutableStateOf(loadXP())
        private set

    var coins by mutableStateOf(loadCoins())
        private set

    var completedTasks by mutableStateOf(loadCompletedTasks())
        private set

    private var _pets = mutableStateOf<List<Pet>>(loadPets())
    val pets: List<Pet> get() = _pets.value

    private fun getRequiredXPForLevel(level: Int): Int {
        return (level + 1) * 100
    }

    private fun loadUsername(): String {
        return sharedPreferences.getString("username", "User") ?: "User"
    }

    private fun loadLevel(): Int {
        return sharedPreferences.getInt("level", 1)
    }

    private fun loadXP(): Int {
        return sharedPreferences.getInt("xp", 0)
    }

    private fun loadCoins(): Int {
        return sharedPreferences.getInt("coins", 200)  // Changed default value to 200
    }

    private fun loadPets(): List<Pet> {
        val petsJson = sharedPreferences.getString("pets", "[]")
        val type = object : TypeToken<List<Pet>>() {}.type
        return gson.fromJson(petsJson, type) ?: emptyList()
    }

    private fun loadCompletedTasks(): Int {
        return sharedPreferences.getInt("completed_tasks", 0)
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

    fun reducePetHealth(petId: Int, amount: Int) {
        _pets.value = _pets.value.map { pet ->
            if (pet.id == petId) {
                val newHealth = (pet.healthPoints - amount).coerceAtLeast(0)
                pet.copy(
                    healthPoints = newHealth,
                    status = if (newHealth < pet.maxHealthPoints) "Sick" else "Healthy"
                )
            } else pet
        }
        savePets()
    }

    fun healPet(petId: Int, healingPoints: Int) {
        _pets.value = _pets.value.map { pet ->
            if (pet.id == petId) {
                val newHealth = (pet.healthPoints + healingPoints).coerceAtMost(pet.maxHealthPoints)
                pet.copy(
                    healthPoints = newHealth,
                    status = if (newHealth < pet.maxHealthPoints) "Sick" else "Healthy"
                )
            } else pet
        }
        savePets()
    }

    fun getPetHealth(petId: Int): Pair<Int, Int>? {
        return pets.find { it.id == petId }?.let {
            Pair(it.healthPoints, it.maxHealthPoints)
        }
    }

    fun addXPAndCoins(xpAmount: Int = 20, coinsAmount: Int = 15) {
        // Update XP and potentially level up
        currentXP += xpAmount
        while (currentXP >= getRequiredXPForLevel(currentLevel)) {
            currentXP -= getRequiredXPForLevel(currentLevel)
            currentLevel++
        }

        // Update coins
        coins += coinsAmount

        // Save all changes
        sharedPreferences.edit()
            .putInt("xp", currentXP)
            .putInt("level", currentLevel)
            .putInt("coins", coins)
            .apply()
    }

    fun incrementCompletedTasks() {
        completedTasks++
        sharedPreferences.edit().putInt("completed_tasks", completedTasks).apply()
    }

    fun addCoins(amount: Int) {
        coins += amount
        sharedPreferences.edit().putInt("coins", coins).apply()
    }

    fun spendCoins(amount: Int): Boolean {
        if (coins >= amount) {
            coins -= amount
            sharedPreferences.edit().putInt("coins", coins).apply()
            return true
        }
        return false
    }

    private fun savePets() {
        sharedPreferences.edit()
            .putString("pets", gson.toJson(_pets.value))
            .apply()
    }
}
