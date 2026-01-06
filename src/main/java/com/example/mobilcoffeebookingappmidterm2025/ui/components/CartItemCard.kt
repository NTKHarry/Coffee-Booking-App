package com.example.mobilcoffeebookingappmidterm2025.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobilcoffeebookingappmidterm2025.model.CartItem
import com.example.mobilcoffeebookingappmidterm2025.ui.CoffeeAvatar
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemCard(
    item: CartItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(15.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CoffeeAvatar(
                    coffee = item.product,
                    width = 80.dp,
                    height = 80.dp,
                )
                Column {
                    Text(
                        text = item.product,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "${item.option.shot} | ${item.option.temperature} | ${item.option.size} | ${item.option.ice}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Colors.onBackground2,
                            fontWeight = FontWeight.Light,
                        ),
                    )
                    Text(
                        text = "x ${item.option.quantity}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Colors.onBackground2,
                            fontWeight = FontWeight.SemiBold
                        ),
                    )
                }
            }
            Text(
                text = "$${String.format("%.2f", item.price)}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.width(60.dp),
                maxLines = 1,
                textAlign = TextAlign.End,
            )
        }
    }
}
