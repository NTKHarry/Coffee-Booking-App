package com.example.mobilcoffeebookingappmidterm2025.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.model.ProductOption
import com.example.mobilcoffeebookingappmidterm2025.model.ShotType
import com.example.mobilcoffeebookingappmidterm2025.model.TemperatureType
import com.example.mobilcoffeebookingappmidterm2025.model.SizeType
import com.example.mobilcoffeebookingappmidterm2025.model.IceType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: MainRepository
) : ViewModel() {

    // Get Name from Repo
    val fullName = repository.observeFullName().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "Loading..."
    )

    // Get Stamps from Repo
    val stampCount = repository.observeStampCount().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    // Get Products from Repo
    val products: List<String> = repository.getProducts()
    val categories: List<String> = repository.getCategories()
    val productsWithCategory: List<MainRepository.Product> = repository.getProductsWithCategory()

    fun resetStampCount() {
        viewModelScope.launch {
            repository.resetStampCount()
        }
    }

    /**
     * Add product to cart. If no option is provided, use sensible defaults
     * (1x, single shot, iced, medium, full ice).
     */
    fun addToCart(product: String, option: ProductOption = ProductOption(
        quantity = 1,
        shot = ShotType.SINGLE,
        temperature = TemperatureType.ICED,
        size = SizeType.MEDIUM,
        ice = IceType.FULL
    )) {
        repository.addToCart(product, option)
    }

    // --- FACTORY (Required to pass Repository to ViewModel manually) ---
    class Factory(private val repository: MainRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}