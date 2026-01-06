package com.example.mobilcoffeebookingappmidterm2025.model

enum class OrderStatus {
    ONGOING,
    COMPLETED
}

data class Order(
    val id: String,
    val product: String,
    val datetime: String,
    val price: Double,
    val address: String,
    // Persist the selected product options so we can show exact ordered options later
    val option: ProductOption,
    // Persist the payment method (e.g., "VISA" or "CASH") and optional coupon percent
    val paymentMethod: String? = null,
    val couponPercent: Int = 0,
    val status: OrderStatus = OrderStatus.ONGOING
)
