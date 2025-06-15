package com.taskive.model

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
        status = if (healthPoints < maxHealthPoints) "Sick" else "Healthy"
    }

    fun getCurrentImage(): Int {
        return if (status == "Sick" && sickImageResId != null) sickImageResId!! else imageResId
    }
}
