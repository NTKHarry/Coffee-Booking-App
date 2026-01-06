package com.example.mobilcoffeebookingappmidterm2025.ui.redeem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class RedeemViewModel(
    private val repository: MainRepository
) : ViewModel() {
    val redeemables = repository.observeRedeemables().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    val availableVouchers = repository.observeAvailableVouchers().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    val ownedVouchers = repository.observeOwnedVouchers().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    val points = repository.observePoints().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    fun redeemDrink(redeemableId: String): Boolean {
        return repository.redeemDrink(redeemableId)
    }
    
    fun purchaseVoucher(voucherId: String): Boolean {
        return repository.purchaseVoucher(voucherId)
    }

    class Factory(
        private val repository: MainRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RedeemViewModel(repository) as T
        }
    }
}
