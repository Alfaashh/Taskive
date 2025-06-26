package com.taskive.model

import com.taskive.R

data class Pet(
    val id: Int,
    val name: String,
    val imageResId: Int,
    var healthPoints: Int,
    val maxHealthPoints: Int,
    var status: String = "Healthy",
    var sickImageResId: Int? = null
) {
    fun updateStatus() {
        status = when {
            healthPoints <= 0 -> "Dead"
            healthPoints < maxHealthPoints -> "Sick"
            else -> "Healthy"
        }
    }

    fun getCurrentImage(): Int {
        return when (status) {
            "Dead" -> R.drawable.death_pet
            "Sick" -> sickImageResId ?: imageResId
            else -> imageResId
        }
    }

    fun isUsable(): Boolean {
        return status != "Dead"
    }
}
