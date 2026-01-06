package com.example.mobilcoffeebookingappmidterm2025.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.content.res.ResourcesCompat
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobilcoffeebookingappmidterm2025.R

@Composable
fun CoffeeAvatar(
    coffee: String,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    // Normalize the coffee name to a drawable resource name: lowercase, non-alnum -> underscore
    val resourceName = coffee.trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')

    // Try to resolve a drawable resource with several candidate names at runtime
    val candidates = listOf(
        resourceName,
        // try without underscores (e.g., "flat_white" -> "flatwhite")
        resourceName.replace("_", ""),
        // try dash variant
        resourceName.replace("_", "-"),
    ).distinct()

    var resolvedId = 0
    for (cand in candidates) {
        if (cand.isBlank()) continue
        val id = ctx.resources.getIdentifier(cand, "drawable", ctx.packageName)
        if (id != 0) {
            resolvedId = id
            break
        }
    }

    if (resolvedId != 0) {
        // Drawable with matching name exists in res/drawable
        Image(
            painter = painterResource(id = resolvedId),
            contentDescription = null,
            modifier = modifier.width(width).height(height),
            contentScale = ContentScale.Inside
        )
    } else {
        // Fallback: show text placeholder until an image is added
        Box(
            modifier = modifier
                .width(width)
                .height(height)
                .background(Color(0xFFE0E0E0))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = coffee,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                textAlign = TextAlign.Center,
                color = Color(0xFF424242),
                maxLines = 3
            )
        }
    }
}

