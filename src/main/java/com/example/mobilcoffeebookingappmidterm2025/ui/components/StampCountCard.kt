package com.example.mobilcoffeebookingappmidterm2025.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mobilcoffeebookingappmidterm2025.R
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors
import java.lang.Integer.min
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StampCountCard(
    stampCount: Int,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    containerVariantColor: Color,
    activeCupTint: Color,
    inactiveCupTint: Color = Colors.inactive,
    onClick: () -> Unit
) {
    val stampCount = min(abs(stampCount), 8)
    val isResetEnabled = stampCount >= 8

    // If not enabled, the card remains visually the same but is not clickable.
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp, 15.dp, 20.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Loyalty card",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = contentColor
                    )
                )
                Text(
                    text = "$stampCount / 8",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = contentColor
                    )
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        // Apply clickable only when the stamp card has reached max
                        if (isResetEnabled) Modifier.clickable { onClick() } else Modifier
                    ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = containerVariantColor,
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // add cup icon for active stamps
                    for (i in 1..stampCount) {
                        Icon(
                            painter = painterResource(R.drawable.cup_glow_light),
                            tint = activeCupTint,
                            contentDescription = null,
                        )
                    }
                    // add cup icon for remaining slots
                    for (i in 1..(8 - stampCount)) {
                        Icon(
                            painter = painterResource(R.drawable.cup_noglow),
                            tint = inactiveCupTint,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}
