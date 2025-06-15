package com.taskive.model

data class StoreItem(
    val id: Int,
    val name: String,
    val price: Int,
    val imageRes: Int,
    val healthPoints: Int = 0,
    val sickImageRes: Int? = null,
    val healingPoints: Int = 0
)
