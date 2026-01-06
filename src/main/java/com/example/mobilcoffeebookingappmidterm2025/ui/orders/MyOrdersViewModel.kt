package com.example.mobilcoffeebookingappmidterm2025.ui.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

enum class OrderTab(val title: String) {
    ONGOING("Ongoing"),
    HISTORY("History"),
}

class MyOrdersViewModel(
    private val repository: MainRepository
) : ViewModel() {
    
    val ongoing = repository.observeOngoingOrders().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    val history = repository.observeHistoryOrders().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    var currentTab by mutableStateOf(OrderTab.ONGOING)
        private set

    fun setTab(tab: OrderTab) {
        currentTab = tab
    }

    fun moveToHistory(orderId: String) {
        repository.moveToHistory(orderId)
    }

    fun moveToOngoing(orderId: String) {
        repository.moveToOngoing(orderId)
    }

    class Factory(
        private val repository: MainRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MyOrdersViewModel(repository) as T
        }
    }
}
