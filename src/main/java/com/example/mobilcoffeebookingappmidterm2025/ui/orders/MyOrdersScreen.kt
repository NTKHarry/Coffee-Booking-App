package com.example.mobilcoffeebookingappmidterm2025.ui.orders

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilcoffeebookingappmidterm2025.model.Order
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.model.CartItem
import com.example.mobilcoffeebookingappmidterm2025.model.ProductOption
import com.example.mobilcoffeebookingappmidterm2025.model.ShotType
import com.example.mobilcoffeebookingappmidterm2025.model.TemperatureType
import com.example.mobilcoffeebookingappmidterm2025.model.SizeType
import com.example.mobilcoffeebookingappmidterm2025.model.IceType
import com.example.mobilcoffeebookingappmidterm2025.ui.UIConfig
import com.example.mobilcoffeebookingappmidterm2025.ui.components.BottomBar
import com.example.mobilcoffeebookingappmidterm2025.ui.components.BottomBarTab
import com.example.mobilcoffeebookingappmidterm2025.ui.components.OrderCard
import com.example.mobilcoffeebookingappmidterm2025.ui.components.CartItemCard
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onBottomBarClick: (BottomBarTab) -> Unit,
    myOrdersViewModel: MyOrdersViewModel = viewModel(factory = MyOrdersViewModel.Factory(com.example.mobilcoffeebookingappmidterm2025.data.MainRepository))
) {
    val ongoing by myOrdersViewModel.ongoing.collectAsState()
    val history by myOrdersViewModel.history.collectAsState()
    val currentTab = myOrdersViewModel.currentTab

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Orders",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            BottomBar(
                tabs = BottomBarTab.values(),
                currentTab = BottomBarTab.ORDERS,
                containerColor = Colors.homeCardVariantContainer,
                onTabClick = onBottomBarClick
            )
        },
    ) { innerPadding ->
        val screenModifier = Modifier.padding(
            innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            innerPadding.calculateTopPadding(),
            innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            0.dp,   
        )
        MyOrdersScreenContent(
            ongoing = ongoing,
            history = history,
            currentTab = currentTab,
            onSetTab = { myOrdersViewModel.setTab(it) },
            onConfirmReceived = { id -> myOrdersViewModel.moveToHistory(id) },
            onDeleteHistory = { id -> MainRepository.removeHistoryOrder(id) },
            modifier = screenModifier
        )
    }
}

@Composable
fun MyOrdersScreenContent(
    ongoing: List<Order>,
    history: List<Order>,
    currentTab: OrderTab,
    onSetTab: (OrderTab) -> Unit,
    onConfirmReceived: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            OrderTab.values().forEach { tab ->
                val selected = tab == currentTab
                Tab(
                    selected = selected,
                    onClick = { onSetTab(tab) },
                ) {
                    Text(
                        text = tab.title,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Colors.inactive
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
        
        when (currentTab) {
            OrderTab.ONGOING -> OrderTabContent(
                orders = ongoing.asReversed(),
                isOngoingTab = true,
                onConfirmReceived = onConfirmReceived,
                onDeleteHistory = onDeleteHistory,
                modifier = Modifier.padding(bottom = UIConfig.SCREEN_BOTTOM_PADDING)
            )
            OrderTab.HISTORY -> OrderTabContent(
                orders = history.asReversed(),
                isOngoingTab = false,
                onConfirmReceived = onConfirmReceived,
                onDeleteHistory = onDeleteHistory,
                modifier = Modifier.padding(bottom = UIConfig.SCREEN_BOTTOM_PADDING)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OrderTabContent(
    orders: List<Order>,
    isOngoingTab: Boolean,
    onConfirmReceived: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only one expanded item at a time
    var expandedOrderId by remember { mutableStateOf<String?>(null) }

    val name by MainRepository.observeFullName().collectAsState()
    val phone by MainRepository.observePhoneNumber().collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(items = orders, key = { it.id }) { order ->
            Column(modifier = Modifier.padding(horizontal = UIConfig.SCREEN_SIDE_PADDING, vertical = 8.dp)) {
                Box(modifier = Modifier
                    .clickable {
                        expandedOrderId = if (expandedOrderId == order.id) null else order.id
                    }
                ) {
                    OrderCard(order)
                }

                AnimatedVisibility(
                    visible = expandedOrderId == order.id,
                    enter = expandVertically(animationSpec = tween(durationMillis = 250)),
                    exit = shrinkVertically(animationSpec = tween(durationMillis = 200))
                ) {
                    // Curved card container for the slide-down panel to make it more beautiful
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            // slight top margin so the panel separates from the item above
                            .padding(start = 4.dp, end = 4.dp, top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                        ) {
                            Text(text = "Name: $name", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Phone: $phone", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Delivery: ${order.address}", style = MaterialTheme.typography.bodyMedium)

                            // Reuse CartItemCard for visual consistency and show the actual ordered options
                            CartItemCard(item = CartItem(id = order.id, product = order.product, price = order.price, option = order.option))

                            Text(text = "Subtotal: $${String.format("%.2f", order.price)}", style = MaterialTheme.typography.bodyMedium)
                            
                            // Show voucher discount if applied
                            if (order.couponPercent > 0) {
                                Text(
                                    text = "Voucher applied: ${order.couponPercent}% off",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Text(text = "Payment method: ${order.paymentMethod ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (isOngoingTab) {
                                    Button(onClick = {
                                        onConfirmReceived(order.id)
                                        expandedOrderId = null
                                    }) {
                                        Text(text = "Confirm Received")
                                    }
                                } else {
                                    Button(onClick = {
                                        onDeleteHistory(order.id)
                                        expandedOrderId = null
                                    }) {
                                        Text(text = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
