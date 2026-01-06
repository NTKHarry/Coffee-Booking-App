package com.example.mobilcoffeebookingappmidterm2025.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobilcoffeebookingappmidterm2025.R
import com.example.mobilcoffeebookingappmidterm2025.model.Order
import com.example.mobilcoffeebookingappmidterm2025.ui.CoffeeAvatar
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@Composable
fun OrderCard(
    order: Order,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            CoffeeAvatar(
                coffee = order.product,
                width = 70.dp,
                height = 70.dp
            )
            
            // Order details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = order.datetime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Colors.onBackground2
                    ),
                )
                Text(
                    text = order.product,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.location_orders),
                        contentDescription = null,
                        tint = Colors.onBackground2
                    )
                    Text(
                        text = order.address,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Colors.onBackground2
                        )
                    )
                }
            }
            
            // Price
            Text(
                text = "$${String.format("%.2f", order.price)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Colors.boldPrimary,
                    fontWeight = FontWeight.Bold
                ),
            )
        }
    }
}
