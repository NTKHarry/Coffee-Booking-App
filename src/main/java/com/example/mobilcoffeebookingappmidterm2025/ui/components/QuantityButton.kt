package com.example.mobilcoffeebookingappmidterm2025.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun QuantityButton(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    width: Dp,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.width(width),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(32.dp),
            ) {
                Icon(
                    Icons.Rounded.Remove,
                    contentDescription = "decrease quantity",
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                modifier = Modifier
                    .align(Alignment.Center)
            )
            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(32.dp),
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "increase quantity",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
