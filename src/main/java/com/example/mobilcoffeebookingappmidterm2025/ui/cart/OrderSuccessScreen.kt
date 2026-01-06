package com.example.mobilcoffeebookingappmidterm2025.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobilcoffeebookingappmidterm2025.R
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@Composable
fun OrderSuccessScreen(
    onTrackOrderClick: () -> Unit,
    onReturnHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.takeaway),
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Order Success",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            ),
        )
        Text(
            text = "Your order has been placed successfully.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Colors.onBackground2
            ),
        )
        Text(
            text = "For more details, go to my orders.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Colors.onBackground2
            ),
        )
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = onTrackOrderClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Track My Order",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onReturnHomeClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Return Home",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
