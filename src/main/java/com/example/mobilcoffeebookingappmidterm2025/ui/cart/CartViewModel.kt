package com.example.mobilcoffeebookingappmidterm2025.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CartUIState(
    val checkOutSucceeded: Boolean = false
)

class CartViewModel(
    private val repository: MainRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUIState())
    val uiState = _uiState.asStateFlow()

    val items = repository.observeCart().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun removeFromCart(itemId: String): Boolean {
        return viewModelScope.run {
            repository.removeFromCart(itemId)
        }
    }

    fun checkOut(): Boolean {
        return viewModelScope.run {
            if (repository.checkOut()) {
                _uiState.update { CartUIState(checkOutSucceeded = true) }
                true
            } else {
                false
            }
        }
    }

    class Factory(
        private val repository: MainRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CartViewModel(repository) as T
        }
    }
}
