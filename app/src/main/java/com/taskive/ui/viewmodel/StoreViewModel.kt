package com.taskive.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.taskive.ui.store.StoreItem

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("taskive_store", Context.MODE_PRIVATE)

    private val _coins = mutableStateOf(sharedPreferences.getInt("user_coins", 200))
    val coins: State<Int> = _coins

    private val _purchasedItems = mutableStateOf<List<StoreItem>>(emptyList())
    val purchasedItems: State<List<StoreItem>> = _purchasedItems

    fun addCoins(amount: Int) {
        _coins.value += amount
        saveCoins()
    }

    fun purchaseItem(item: StoreItem): Boolean {
        if (_coins.value >= item.price) {
            _coins.value -= item.price
            val currentItems = _purchasedItems.value.toMutableList()
            currentItems.add(item)
            _purchasedItems.value = currentItems
            saveCoins()
            savePurchasedItems()
            return true
        }
        return false
    }

    private fun saveCoins() {
        sharedPreferences.edit()
            .putInt("user_coins", _coins.value)
            .apply()
    }

    private fun savePurchasedItems() {
        sharedPreferences.edit()
            .putInt("purchased_items_count", _purchasedItems.value.size)
            .apply()
    }

    // Call this when a task is completed to reward the user
    fun rewardTaskCompletion() {
        addCoins(10) // Reward 10 coins for completing a task
    }
}
