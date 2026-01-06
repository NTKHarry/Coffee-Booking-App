package com.example.mobilcoffeebookingappmidterm2025.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobilcoffeebookingappmidterm2025.model.Coupon
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@Composable
fun VoucherSlot(
    voucher: Coupon,
    pointsCost: Int,
    ownedQuantity: Int = 0,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onButtonClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f), // Take remaining space between card and button
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Voucher ${voucher.label}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "Use on your next purchase",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Colors.onBackground2
                ),
                maxLines = 2 // Allow text to wrap to new line before touching button
            )
            if (ownedQuantity > 0) {
                Text(
                    text = "Owned: $ownedQuantity",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                )
            }
        }
        // Button at the same height level as the card
        Button(
            onClick = onButtonClick,
            shape = RoundedCornerShape(50.dp),
            contentPadding = PaddingValues(17.dp, 0.dp),
            enabled = enabled,
        ) {
            Text(
                text = "$pointsCost pts",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

