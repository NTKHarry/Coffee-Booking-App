package com.example.mobilcoffeebookingappmidterm2025.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.model.IceType
import com.example.mobilcoffeebookingappmidterm2025.model.ProductOption
import com.example.mobilcoffeebookingappmidterm2025.model.ShotType
import com.example.mobilcoffeebookingappmidterm2025.model.SizeType
import com.example.mobilcoffeebookingappmidterm2025.model.TemperatureType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class DetailsViewModel(
    private val repository: MainRepository,
    val product: String
) : ViewModel() {
    
    private val _option = MutableStateFlow(
        ProductOption(
            quantity = 1,
            shot = ShotType.SINGLE,
            temperature = TemperatureType.ICED,
            size = SizeType.MEDIUM,
            ice = IceType.FULL
        )
    )
    val option = _option.asStateFlow()

    // Make price a StateFlow that automatically updates when option changes
    val price: StateFlow<Double> = option.map { currentOption ->
        repository.getPrice(product, currentOption)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.getPrice(product, option.value)
    )

    // Expose cart items for preview without navigation
    val cart: StateFlow<List<com.example.mobilcoffeebookingappmidterm2025.model.CartItem>> = 
        repository.observeCart()

    fun incQuantity() {
        _option.update { it.copy(quantity = it.quantity + 1) }
    }

    fun decQuantity() {
        _option.update { it.copy(quantity = maxOf(it.quantity - 1, 1)) }
    }

    fun setShot(shot: ShotType) {
        _option.update { it.copy(shot = shot) }
    }

    fun setTemperature(temperature: TemperatureType) {
        _option.update { it.copy(temperature = temperature) }
    }

    fun setSize(size: SizeType) {
        _option.update { it.copy(size = size) }
    }

    fun setIce(ice: IceType) {
        _option.update { it.copy(ice = ice) }
    }

    fun addToCart() {
        repository.addToCart(product, option.value)
    }

    /**
     * Reset the option state to defaults. Call this when the details view is closed
     * (for example when user presses back or after adding to cart) to ensure the
     * next time the screen is opened it starts fresh.
     */
    fun reset() {
        _option.value = ProductOption(
            quantity = 1,
            shot = ShotType.SINGLE,
            temperature = TemperatureType.ICED,
            size = SizeType.MEDIUM,
            ice = IceType.FULL
        )
    }

    class Factory(
        private val repository: MainRepository,
        private val product: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailsViewModel(repository, product) as T
        }
    }
}
