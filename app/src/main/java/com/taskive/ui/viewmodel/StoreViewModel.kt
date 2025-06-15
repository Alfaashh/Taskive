package com.taskive.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.taskive.R
import com.taskive.model.StoreItem
import com.taskive.model.Pet

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("taskive_store", Context.MODE_PRIVATE)

    private val _coins = mutableStateOf(sharedPreferences.getInt("user_coins", 200))
    val coins: State<Int> = _coins

    private val _purchasedPetIds = mutableStateOf<List<Int>>(loadPurchasedPets())
    val purchasedPetIds: State<List<Int>> = _purchasedPetIds

    // Restoring StoreViewModel to use the original GIF files
    val availablePets = listOf(
        StoreItem(1, "Cat", 250, R.drawable.cat, healthPoints = 250, sickImageRes = R.drawable.sick_cat),
        StoreItem(2, "Penguin", 200, R.drawable.penguin, healthPoints = 200, sickImageRes = R.drawable.sick_penguin)
    )

    val availableFoods = listOf(
        StoreItem(3, "Sushi", 50, R.drawable.sushi, healingPoints = 20),
        StoreItem(4, "Tomato", 30, R.drawable.tomato, healingPoints = 10)
    )

    private val _showHealDialog = mutableStateOf(false)
    val showHealDialog: State<Boolean> = _showHealDialog

    private val _selectedFood = mutableStateOf<StoreItem?>(null)
    val selectedFood: State<StoreItem?> = _selectedFood

    init {
        // Initialize purchased pets from shared preferences
        val savedPets = sharedPreferences.getString("purchased_pets", null)
        if (!savedPets.isNullOrEmpty()) {
            _purchasedPetIds.value = savedPets.split(",").map { it.toInt() }
        }
    }

    fun purchaseItem(item: StoreItem): Boolean {
        if (_coins.value >= item.price) {
            _coins.value -= item.price
            saveCoins()
            return true
        }
        return false
    }

    fun buyPet(itemId: Int, userViewModel: UserViewModel) {
        val item = availablePets.find { it.id == itemId }
        if (item != null && _coins.value >= item.price && !_purchasedPetIds.value.contains(item.id)) {
            _coins.value -= item.price
            _purchasedPetIds.value = _purchasedPetIds.value + item.id

            userViewModel.addPet(
                Pet(
                    id = item.id,
                    name = item.name,
                    imageResId = item.imageRes,
                    healthPoints = item.healthPoints,
                    maxHealthPoints = item.healthPoints,
                    sickImageResId = item.sickImageRes
                )
            )

            saveCoins()
            savePurchasedPets()
        }
    }

    fun buyFood(itemId: Int, userViewModel: UserViewModel) {
        val item = availableFoods.find { it.id == itemId }
        if (item != null && _coins.value >= item.price) {
            _selectedFood.value = item
            _showHealDialog.value = true
        }
    }

    fun healPet(petId: Int, userViewModel: UserViewModel) {
        val food = _selectedFood.value
        if (food != null) {
            _coins.value -= food.price
            saveCoins()
            userViewModel.healPet(petId, food.healingPoints)
            _showHealDialog.value = false
            _selectedFood.value = null
        }
    }

    private fun saveCoins() {
        sharedPreferences.edit()
            .putInt("user_coins", _coins.value)
            .apply()
    }

    private fun savePurchasedPets() {
        sharedPreferences.edit()
            .putString("purchased_pets", _purchasedPetIds.value.joinToString(","))
            .apply()
    }

    private fun loadPurchasedPets(): List<Int> {
        val savedPets = sharedPreferences.getString("purchased_pets", "") ?: ""
        return if (savedPets.isNotEmpty()) {
            savedPets.split(",").map { it.toInt() }
        } else {
            emptyList()
        }
    }

    fun addCoins(amount: Int) {
        _coins.value += amount
        saveCoins()
    }

    fun dismissHealDialog() {
        _showHealDialog.value = false
        _selectedFood.value = null
    }
}
