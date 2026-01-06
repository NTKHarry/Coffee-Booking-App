package com.example.mobilcoffeebookingappmidterm2025.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobilcoffeebookingappmidterm2025.R
import kotlinx.coroutines.delay

/**
 * Splash/Idle Screen shown on app startup
 * Displays coffee logo, app name "Ordinary Coffee House", and slogan "Sweet and bitterness"
 * Uses splash_background.xml drawable for the gradient background
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // Auto-navigate after 2.5 seconds
    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

    // Use drawable image background (idle.jpg) and overlay content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.idle),
            contentDescription = "Idle background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Overlay content on top of the background image
            // Coffee icon as logo (slightly translucent to blend with background)
            Icon(
                imageVector = Icons.Default.Coffee,
                contentDescription = "Coffee Logo",
                tint = Color(0xCCFFF8E1), // Slightly translucent cream
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // App name: Ordinary Coffee House (white for maximum contrast)
            Text(
                text = "The Code Cup",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFFFF),
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Slogan: Sweet and bitterness (white)
            Text(
                text = "Sweet and bitterness",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFFFFFF),
                textAlign = TextAlign.Center,
                letterSpacing = 1.0.sp
            )
        }
    }
}
