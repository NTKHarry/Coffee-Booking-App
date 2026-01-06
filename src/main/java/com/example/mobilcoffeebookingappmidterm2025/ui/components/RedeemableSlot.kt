package com.example.mobilcoffeebookingappmidterm2025.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobilcoffeebookingappmidterm2025.model.Redeemable
import com.example.mobilcoffeebookingappmidterm2025.ui.CoffeeAvatar
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@Composable
fun RedeemableSlot(
    redeemable: Redeemable,
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
        CoffeeAvatar(
            coffee = redeemable.product,
            width = 80.dp,
            height = 80.dp,
        )
        Column(
            modifier = Modifier.weight(1f), // Take remaining space between avatar and button
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = redeemable.product,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "Valid until ${redeemable.validUntil}",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Colors.onBackground2
                ),
                maxLines = 2 // Allow text to wrap to new line before touching button
            )
        }
        // Button at the same height level as the avatar
        Button(
            onClick = onButtonClick,
            shape = RoundedCornerShape(50.dp),
            contentPadding = PaddingValues(17.dp, 0.dp),
            enabled = enabled,
        ) {
            Text(
                text = "${redeemable.pointsRequired} pts",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
