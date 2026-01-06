package com.example.mobilcoffeebookingappmidterm2025.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilcoffeebookingappmidterm2025.R
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.model.IceType
import com.example.mobilcoffeebookingappmidterm2025.model.ProductOption
import com.example.mobilcoffeebookingappmidterm2025.model.ShotType
import com.example.mobilcoffeebookingappmidterm2025.model.SizeType
import com.example.mobilcoffeebookingappmidterm2025.model.TemperatureType
import com.example.mobilcoffeebookingappmidterm2025.ui.CoffeeAvatar
import com.example.mobilcoffeebookingappmidterm2025.ui.UIConfig
import com.example.mobilcoffeebookingappmidterm2025.ui.components.QuantityButton
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    product: String,
    onBackClick: () -> Unit,
    onAddToCartNavigate: () -> Unit,
    onArPreviewClick: () -> Unit = {},
    // Use the product as key so a different ViewModel is created for different products.
    // This prevents option state from one product leaking into another product's detail screen.
    detailsViewModel: DetailsViewModel = viewModel(key = product, factory = DetailsViewModel.Factory(MainRepository, product))
) {
    val option by detailsViewModel.option.collectAsState()
    val totalPrice by detailsViewModel.price.collectAsState()
    val cartItems by detailsViewModel.cart.collectAsState()
    
    var showCartPreview by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Reset view state and navigate back
                            detailsViewModel.reset()
                            onBackClick()
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back_arrow),
                            contentDescription = "back",
                        )
                    }
                },
                actions = {
                    IconButton(
                            onClick = {
                                // Show cart preview instead of navigating
                                showCartPreview = true
                            },
                        ) {
                        Icon(
                            painter = painterResource(R.drawable.cart),
                            contentDescription = "view cart preview",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier.padding(horizontal = UIConfig.TOP_BAR_SIDE_PADDING),
            )
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        DetailsScreenContent(
            product = product,
            option = option,
            totalPrice = totalPrice,
            onIncQuantity = { detailsViewModel.incQuantity() },
            onDecQuantity = { detailsViewModel.decQuantity() },
            onSetShot = { detailsViewModel.setShot(it) },
            onSetTemperature = { detailsViewModel.setTemperature(it) },
            onSetSize = { detailsViewModel.setSize(it) },
            onSetIce = { detailsViewModel.setIce(it) },
            onAddToCart = {
                detailsViewModel.addToCart()
                // Reset details state so the next time the screen is opened it starts fresh
                detailsViewModel.reset()
                onAddToCartNavigate() // Navigate to cart after adding
            },
            onArPreviewClick = onArPreviewClick,
            modifier = screenModifier.padding(
                UIConfig.SCREEN_SIDE_PADDING,
                0.dp,
                UIConfig.SCREEN_SIDE_PADDING,
                15.dp
            )
        )
        
        // Show cart preview modal when triggered
        if (showCartPreview) {
            CartPreviewSheet(
                cartItems = cartItems,
                onDismiss = { showCartPreview = false },
                onViewFullCart = onAddToCartNavigate
            )
        }
    }
}

@Composable
fun DetailsScreenContent(
    product: String,
    option: ProductOption,
    totalPrice: Double,
    onIncQuantity: () -> Unit,
    onDecQuantity: () -> Unit,
    onSetShot: (ShotType) -> Unit,
    onSetTemperature: (TemperatureType) -> Unit,
    onSetSize: (SizeType) -> Unit,
    onSetIce: (IceType) -> Unit,
    onAddToCart: () -> Unit,
    onArPreviewClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val activeColor = MaterialTheme.colorScheme.onBackground
            val inactiveColor = Colors.inactive
            
            // Product Image Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CoffeeAvatar(coffee = product, width = 200.dp, height = 150.dp)
                }
            }
            
            // Product Name + Quantity
            OptionRow(
                title = product,
                modifier = Modifier.height(48.dp)
            ) {
                QuantityButton(
                    quantity = option.quantity,
                    onIncrease = onIncQuantity,
                    onDecrease = onDecQuantity,
                    width = 85.dp,
                    modifier = Modifier.height(36.dp),
                )
            }
            
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )
            
            // Shot Option
            OptionRow(
                title = "Shot",
                modifier = Modifier.height(48.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val current = option.shot
                    OutlinedButton(
                        onClick = { onSetShot(ShotType.SINGLE) },
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(
                            1.2.dp,
                            if (current == ShotType.SINGLE) activeColor else inactiveColor
                        ),
                        contentPadding = PaddingValues(20.dp, 0.dp),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(
                            text = "Single",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                        )
                    }
                    OutlinedButton(
                        onClick = { onSetShot(ShotType.DOUBLE) },
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(
                            1.2.dp,
                            if (current == ShotType.DOUBLE) activeColor else inactiveColor
                        ),
                        contentPadding = PaddingValues(20.dp, 0.dp),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(
                            text = "Double",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                        )
                    }
                }
            }
            
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )
            
            // Temperature Option
            OptionRow(
                title = "Select",
                modifier = Modifier.height(48.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val current = option.temperature
                    IconButton(onClick = { onSetTemperature(TemperatureType.HOT) }) {
                        Icon(
                            painter = painterResource(R.drawable.cup_hot),
                            contentDescription = "select hot",
                            tint = if (current == TemperatureType.HOT) activeColor else inactiveColor
                        )
                    }
                    IconButton(onClick = { onSetTemperature(TemperatureType.ICED) }) {
                        Icon(
                            painter = painterResource(R.drawable.cup_iced),
                            contentDescription = "select cold",
                            tint = if (current == TemperatureType.ICED) activeColor else inactiveColor
                        )
                    }
                }
            }
            
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )
            
            // Size Option
            OptionRow(
                title = "Size",
                modifier = Modifier.height(48.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    val current = option.size
                    IconButton(
                        onClick = { onSetSize(SizeType.SMALL) },
                        modifier = Modifier.height(22.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cup_small),
                            contentDescription = "select size small",
                            tint = if (current == SizeType.SMALL) activeColor else inactiveColor
                        )
                    }
                    IconButton(
                        onClick = { onSetSize(SizeType.MEDIUM) },
                        modifier = Modifier.height(31.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cup_medium),
                            contentDescription = "select size medium",
                            tint = if (current == SizeType.MEDIUM) activeColor else inactiveColor
                        )
                    }
                    IconButton(
                        onClick = { onSetSize(SizeType.LARGE) },
                        modifier = Modifier.height(38.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cup_large),
                            contentDescription = "select size large",
                            tint = if (current == SizeType.LARGE) activeColor else inactiveColor
                        )
                    }
                }
            }
            
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )
            
            // Ice Option
            OptionRow(
                title = "Ice",
                modifier = Modifier.height(48.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    val current = option.ice
                    IconButton(onClick = { onSetIce(IceType.LESS) }) {
                        Icon(
                            painter = painterResource(R.drawable.ice1),
                            contentDescription = "select less ice",
                            tint = if (current == IceType.LESS) activeColor else inactiveColor
                        )
                    }
                    IconButton(onClick = { onSetIce(IceType.HALF) }) {
                        Icon(
                            painter = painterResource(R.drawable.ice2),
                            contentDescription = "select half ice",
                            tint = if (current == IceType.HALF) activeColor else inactiveColor
                        )
                    }
                    IconButton(onClick = { onSetIce(IceType.FULL) }) {
                        Icon(
                            painter = painterResource(R.drawable.ice3),
                            contentDescription = "select full ice",
                            tint = if (current == IceType.FULL) activeColor else inactiveColor
                        )
                    }
                }
            }
        }
        
        // Total Amount + Add to Cart Button
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "$${String.format("%.2f", totalPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            
            // AR Preview Button
            OutlinedButton(
                onClick = onArPreviewClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.right_arrow),
                    contentDescription = "AR Preview",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View in AR",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add to cart",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun OptionRow(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartPreviewSheet(
    cartItems: List<com.example.mobilcoffeebookingappmidterm2025.model.CartItem>,
    onDismiss: () -> Unit,
    onViewFullCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cart Preview",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close preview"
                    )
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            if (cartItems.isEmpty()) {
                // Empty cart state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add items to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Cart items list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cartItems.forEach { item ->
                        CartPreviewItem(item = item)
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                
                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total (${cartItems.sumOf { it.option.quantity }} items)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$${String.format("%.2f", cartItems.sumOf { it.price })}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // View Full Cart Button
                Button(
                    onClick = {
                        onDismiss()
                        onViewFullCart()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "View Full Cart",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CartPreviewItem(
    item: com.example.mobilcoffeebookingappmidterm2025.model.CartItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            CoffeeAvatar(
                coffee = item.product,
                width = 60.dp,
                height = 50.dp
            )
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.product,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = "${item.option.size.name} • ${item.option.shot.name} • ${item.option.temperature.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "Qty: ${item.option.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Price
            Text(
                text = "$${String.format("%.2f", item.price)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
