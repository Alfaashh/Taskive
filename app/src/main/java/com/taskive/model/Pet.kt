package com.taskive.model

data class Pet(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val status: String = "Healthy"
)
