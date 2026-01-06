package com.example.mobilcoffeebookingappmidterm2025.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobilcoffeebookingappmidterm2025.model.PointReward
import kotlin.math.abs
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@Composable
fun RewardHistorySlot(
    reward: PointReward,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = reward.product,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = reward.datetime,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Colors.onBackground2
                ),
            )
        }
        val pts = reward.points
        val sign = if (pts < 0) "-" else "+"
        val displayPts = abs(pts)

        Text(
            text = "$sign $displayPts Pts",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
        )
    }
}
