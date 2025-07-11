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

    private var _completedTasks = mutableStateOf(sharedPreferences.getInt("completed_tasks", 0))
    val completedTasks: Int get() = _completedTasks.value

    private var _pets = mutableStateOf<List<Pet>>(loadPets())
    val pets: List<Pet> get() = _pets.value

    private fun getRequiredXPForLevel(level: Int): Int {
        return level * 100
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
        return sharedPreferences.getInt("coins", 500)  // Set default value to 500
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

    fun healPet(petId: Int, healingPoints: Int) {
        _pets.value = _pets.value.map { pet ->
            if (pet.id == petId) {
                // Don't heal if pet is at max health or dead
                if (pet.status == "Dead" || pet.healthPoints >= pet.maxHealthPoints) {
                    pet
                } else {
                    val newHealth = (pet.healthPoints + healingPoints).coerceAtMost(pet.maxHealthPoints)
                    pet.copy(
                        healthPoints = newHealth,
                        status = if (newHealth < pet.maxHealthPoints) "Sick" else "Healthy"
                    )
                }
            } else pet
        }
        savePets()
    }

    fun reducePetHealth(petId: Int, amount: Int) {
        _pets.value = _pets.value.map { pet ->
            if (pet.id == petId) {
                val newHealth = (pet.healthPoints - amount).coerceAtLeast(0)
                pet.copy(
                    healthPoints = newHealth,
                    status = when {
                        newHealth <= 0 -> "Dead"
                        newHealth < pet.maxHealthPoints -> "Sick"
                        else -> "Healthy"
                    }
                )
            } else pet
        }
        savePets()
    }

    fun addXPAndCoins(xpAmount: Int = 20, coinsAmount: Int = 15) {
        // Add XP and check for level up
        currentXP += xpAmount

        // Keep leveling up while XP is sufficient
        while (currentXP >= getRequiredXPForLevel(currentLevel)) {
            currentXP -= getRequiredXPForLevel(currentLevel)
            currentLevel++
        }

        // Add coins
        coins += coinsAmount

        // Save changes
        sharedPreferences.edit()
            .putInt("xp", currentXP)
            .putInt("level", currentLevel)
            .putInt("coins", coins)
            .apply()
    }

    fun incrementCompletedTasks() {
        _completedTasks.value++
        sharedPreferences.edit().putInt("completed_tasks", _completedTasks.value).apply()
    }

    fun updateCompletedTasks(count: Int) {
        _completedTasks.value = count
        sharedPreferences.edit().putInt("completed_tasks", count).apply()
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
