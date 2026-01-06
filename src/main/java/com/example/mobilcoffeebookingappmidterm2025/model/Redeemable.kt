package com.example.mobilcoffeebookingappmidterm2025.model

data class Redeemable(
    val id: String,
    val product: String,
    val validUntil: String,
    val pointsRequired: Int
)
