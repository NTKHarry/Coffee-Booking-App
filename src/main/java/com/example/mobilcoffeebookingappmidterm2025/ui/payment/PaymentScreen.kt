package com.example.mobilcoffeebookingappmidterm2025.ui.payment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilcoffeebookingappmidterm2025.R
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.model.VoucherOwned
import com.example.mobilcoffeebookingappmidterm2025.ui.UIConfig
import com.example.mobilcoffeebookingappmidterm2025.ui.components.CartItemCard
import kotlinx.coroutines.launch
// import logcat
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(factory = PaymentViewModel.Factory(MainRepository))
) {
    val items by paymentViewModel.items.collectAsState()
    val name by paymentViewModel.name.collectAsState()
    val phone by paymentViewModel.phone.collectAsState()
    val coupons by paymentViewModel.coupons.collectAsState()
    val ownedVouchers by paymentViewModel.ownedVouchers.collectAsState()
    val subtotal by paymentViewModel.subtotal.collectAsState()
    val finalPrice by paymentViewModel.finalPrice.collectAsState()
    val repoAddress by paymentViewModel.address.collectAsState()
    val tempAddress by paymentViewModel.tempAddress.collectAsState()
    val selectedMethod by paymentViewModel.selectedPaymentMethod.collectAsState()
    val selectedCouponId by paymentViewModel.selectedCouponId.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var processing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Payment", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painter = painterResource(R.drawable.back_arrow), contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.padding(horizontal = UIConfig.TOP_BAR_SIDE_PADDING)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = UIConfig.SCREEN_SIDE_PADDING, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User info - use fallback if empty (e.g., "Not provided")
            Text(
                text = "Name: ${name.ifEmpty { "Not provided" }}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Phone: ${phone.ifEmpty { "Not provided" }}",
                style = MaterialTheme.typography.bodyLarge
            )

            // Single delivery address field: shows the repo default initially.
            // Tap the pencil to edit temporarily (does not change MainRepository).
            var editingAddress by remember { mutableStateOf(false) }
            if (!editingAddress) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Delivery address", style = MaterialTheme.typography.labelMedium)
                        Text(text = tempAddress, style = MaterialTheme.typography.bodyLarge)
                    }
                    IconButton(onClick = { editingAddress = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "edit address")
                    }
                }
            } else {
                // When editing, allow cancel which reverts to repo address, or save which keeps tempAddress
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tempAddress,
                        onValueChange = { paymentViewModel.tempAddress.value = it },
                        label = { Text(text = "Delivery address") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        // Cancel: revert temporary address to repo's stored address
                        paymentViewModel.tempAddress.value = repoAddress
                        editingAddress = false
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "cancel")
                    }
                    IconButton(onClick = {
                        // Save: keep tempAddress (no repo mutation)
                        editingAddress = false
                    }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "save")
                    }
                }
            }

            // Items
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    CartItemCard(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Subtotal and coupon
            Text(text = "Subtotal: $${String.format("%.2f", subtotal)}", style = MaterialTheme.typography.bodyLarge)

            Text(text = "Select coupon:")
            // Only show vouchers that the user actually owns (quantity > 0)
            val availableVouchers = ownedVouchers.filter { it.quantity > 0 }
            
            if (availableVouchers.isEmpty()) {
                Text(
                    text = "No vouchers available. Purchase vouchers from Rewards screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                availableVouchers.forEach { voucher ->
                    val rowModifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Toggle: if already selected, deselect (set to null), else select this voucher
                            paymentViewModel.selectedCouponId.value =
                                if (selectedCouponId == voucher.voucherId) null else voucher.voucherId
                        }
                        .padding(4.dp)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = rowModifier) {
                        RadioButton(
                            selected = (selectedCouponId == voucher.voucherId),
                            onClick = {
                                paymentViewModel.selectedCouponId.value =
                                    if (selectedCouponId == voucher.voucherId) null else voucher.voucherId
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "${voucher.label} (${voucher.percentOff}%)")
                            Text(
                                text = "Remaining: ${voucher.quantity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Payment method
            Text(text = "Payment method:")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = (selectedMethod == PaymentMethod.CASH),
                    onClick = { paymentViewModel.selectedPaymentMethod.value = PaymentMethod.CASH }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Cash")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = (selectedMethod == PaymentMethod.VISA),
                    onClick = { paymentViewModel.selectedPaymentMethod.value = PaymentMethod.VISA }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Visa")
            }

            // Final price
            Text(text = "Total: $${String.format("%.2f", finalPrice)}", style = MaterialTheme.typography.titleLarge)

            Button(
                onClick = {
                    if (!processing) {
                        processing = true
                        coroutineScope.launch {
                            try {
                                val ok = paymentViewModel.confirmPayment()
                                if (ok) onPaymentSuccess()
                            } catch (e: Exception) {
                                // Defensive: log the exception and avoid crashing the UI.
                                e.printStackTrace()
                                Log.e("Payment", "confirmPayment failed", e)
                            } finally {
                                processing = false
                            }
                        }
                    }
                },
                enabled = items.isNotEmpty() && tempAddress.isNotBlank()
            ) {
                Text(text = "Confirm Payment")
            }
        }
    }
}
