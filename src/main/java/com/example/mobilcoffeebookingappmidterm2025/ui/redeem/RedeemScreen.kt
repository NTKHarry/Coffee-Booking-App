package com.example.mobilcoffeebookingappmidterm2025.ui.redeem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.model.Coupon
import com.example.mobilcoffeebookingappmidterm2025.model.Redeemable
import com.example.mobilcoffeebookingappmidterm2025.model.VoucherOwned
import com.example.mobilcoffeebookingappmidterm2025.ui.UIConfig
import com.example.mobilcoffeebookingappmidterm2025.ui.components.RedeemableSlot
import com.example.mobilcoffeebookingappmidterm2025.ui.components.VoucherSlot
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemScreen(
    onBackClick: () -> Unit,
    redeemViewModel: RedeemViewModel = viewModel(factory = RedeemViewModel.Factory(MainRepository)),
    modifier: Modifier = Modifier
) {
    val redeemables by redeemViewModel.redeemables.collectAsState()
    val vouchers by redeemViewModel.availableVouchers.collectAsState()
    val ownedVouchers by redeemViewModel.ownedVouchers.collectAsState()
    val points by redeemViewModel.points.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Redeem",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier.padding(horizontal = UIConfig.TOP_BAR_SIDE_PADDING),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        RedeemScreenContent(
            redeemables = redeemables,
            vouchers = vouchers,
            ownedVouchers = ownedVouchers,
            currentPoints = points,
            voucherCost = 300,
            onRedeemableClick = { redeemable ->
                val success = redeemViewModel.redeemDrink(redeemable.id)
                scope.launch {
                    if (success) {
                        snackbarHostState.showSnackbar("Successfully redeemed ${redeemable.product}! Check My Orders.")
                    } else {
                        snackbarHostState.showSnackbar("Insufficient points for ${redeemable.product}")
                    }
                }
            },
            onVoucherClick = { voucher ->
                val success = redeemViewModel.purchaseVoucher(voucher.id)
                scope.launch {
                    if (success) {
                        snackbarHostState.showSnackbar("Successfully purchased ${voucher.label} voucher!")
                    } else {
                        snackbarHostState.showSnackbar("Insufficient points for voucher")
                    }
                }
            },
            modifier = screenModifier.padding(UIConfig.SCREEN_SIDE_PADDING, 10.dp, UIConfig.SCREEN_SIDE_PADDING, 0.dp)
        )
    }
}

@Composable
fun RedeemScreenContent(
    redeemables: List<Redeemable>,
    vouchers: List<Coupon>,
    ownedVouchers: List<VoucherOwned>,
    currentPoints: Int,
    voucherCost: Int,
    onRedeemableClick: (Redeemable) -> Unit,
    onVoucherClick: (Coupon) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        // Section: Redeemable Drinks
        item {
            Text(
                text = "Redeemable Drinks",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
        
        items(
            count = redeemables.size,
            key = { index -> redeemables[index].id }
        ) { index ->
            val redeemable = redeemables[index]
            RedeemableSlot(
                redeemable = redeemable,
                enabled = currentPoints >= redeemable.pointsRequired,
                onButtonClick = { onRedeemableClick(redeemable) },
            )
            if (index < redeemables.lastIndex) {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
        
        // Section: Vouchers
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Vouchers",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
        
        items(
            count = vouchers.size,  
            key = { index -> vouchers[index].id }
        ) { index ->
            val voucher = vouchers[index]
            val ownedQuantity = ownedVouchers.find { it.voucherId == voucher.id }?.quantity ?: 0
            
            VoucherSlot(
                voucher = voucher,
                pointsCost = voucherCost,
                ownedQuantity = ownedQuantity,
                enabled = currentPoints >= voucherCost,
                onButtonClick = { onVoucherClick(voucher) },
            )
            if (index < vouchers.lastIndex) {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
