package com.example.mobilcoffeebookingappmidterm2025.model

data class CartItem(
    val id: String,
    val product: String,
    val price: Double,
    val option: ProductOption
)
