package com.example.mobilcoffeebookingappmidterm2025.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.model.Coupon
import com.example.mobilcoffeebookingappmidterm2025.model.VoucherOwned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

enum class PaymentMethod { VISA, CASH }

class PaymentViewModel(private val repository: MainRepository) : ViewModel() {

    val items = repository.observeCart().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val name = repository.observeFullName().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val phone = repository.observePhoneNumber().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val address = repository.observeDeliveryLocation().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val coupons: StateFlow<List<Coupon>> = repository.observeCoupons().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Vouchers the user actually owns (with quantities)
    val ownedVouchers = repository.observeOwnedVouchers().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Temporary editable address (does NOT update repository)
    // Initialize from repository current value to ensure the UI shows repo address initially
    val tempAddress = MutableStateFlow(repository.observeDeliveryLocation().value)

    // Selected payment method and coupon
    val selectedPaymentMethod = MutableStateFlow(PaymentMethod.CASH)
    val selectedCouponId = MutableStateFlow<String?>(null)

    // Subtotal flow
    val subtotal = items.map { list ->
        list.sumOf { it.price }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.0
    )

    // Final price after coupon
    // Compute final price from ownedVouchers directly (they already have percentOff field)
    val finalPrice = combine(subtotal, selectedCouponId, ownedVouchers) { sub, couponId, ownedList ->
        val percent = if (couponId != null) {
            val voucher = ownedList.find { it.voucherId == couponId }
            if (voucher != null && voucher.quantity > 0) voucher.percentOff else 0
        } else {
            0
        }
        sub * (1.0 - percent / 100.0)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.0
    )

    fun confirmPayment(): Boolean {
        // Determine selected coupon percent from ownedVouchers (has percentOff field)
        val couponId = selectedCouponId.value
        val couponPercent = if (couponId != null) {
            val voucher = ownedVouchers.value.find { it.voucherId == couponId }
            if (voucher != null && voucher.quantity > 0) voucher.percentOff else 0
        } else {
            0
        }

        val addressToUse = tempAddress.value
        val method = selectedPaymentMethod.value.name

        // If a coupon was selected and the user owns it, consume it.
        if (couponId != null && couponPercent > 0) {
            repository.useVoucher(couponId)
        }

        return repository.checkOut(addressToUse, couponPercent, method)
    }

    class Factory(private val repository: MainRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PaymentViewModel(repository) as T
        }
    }
}
