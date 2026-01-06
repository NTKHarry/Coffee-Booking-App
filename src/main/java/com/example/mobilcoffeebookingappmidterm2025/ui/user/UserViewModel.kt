package com.example.mobilcoffeebookingappmidterm2025.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class UserViewModel(private val repository: MainRepository) : ViewModel() {

    val fullName = repository.observeFullName().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "Loading..."
    )

    val phoneNumber = repository.observePhoneNumber().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val email = repository.observeEmail().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val deliveryLocation = repository.observeDeliveryLocation().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val avatarResId = repository.observeAvatarResId().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )
    
    val photoUrl = repository.observePhotoUrl().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )
    
    fun logout() {
        repository.logout()
    }

    fun updateUserInfo(
        fullName: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        deliveryLocation: String? = null
    ) {
        repository.updateUserInfo(
            fullName = fullName,
            phoneNumber = phoneNumber,
            email = email,
            deliveryLocation = deliveryLocation
        )
    }

    class Factory(private val repository: MainRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(repository) as T
        }
    }
}
