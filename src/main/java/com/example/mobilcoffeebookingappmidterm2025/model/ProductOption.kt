package com.example.mobilcoffeebookingappmidterm2025.model

enum class ShotType(val value: Int, private val str: String) {
    SINGLE(0, "single"),
    DOUBLE(1, "double");

    override fun toString(): String {
        return str
    }
}

enum class TemperatureType(val value: Int, private val str: String) {
    HOT(0, "hot"),
    ICED(1, "iced");

    override fun toString(): String {
        return str
    }
}

enum class SizeType(val value: Int, private val str: String) {
    SMALL(0, "small"),
    MEDIUM(1, "medium"),
    LARGE(2, "large");

    override fun toString(): String {
        return str
    }
}

enum class IceType(val value: Int, private val str: String) {
    LESS(0, "less ice"),
    HALF(1, "half ice"),
    FULL(2, "full ice");

    override fun toString(): String {
        return str
    }
}

data class ProductOption(
    val quantity: Int,
    val shot: ShotType,
    val temperature: TemperatureType,
    val size: SizeType,
    val ice: IceType
)
