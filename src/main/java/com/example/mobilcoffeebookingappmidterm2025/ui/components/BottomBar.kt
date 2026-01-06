package com.example.mobilcoffeebookingappmidterm2025.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mobilcoffeebookingappmidterm2025.R
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

enum class BottomBarTab(
    val title: String,
    @DrawableRes val iconId: Int
) {
    HOME("Home", R.drawable.home_glow),
    REWARDS("Rewards", R.drawable.rewards_glow),
    ORDERS("My Orders", R.drawable.orders_glow),
}

@Composable
fun BottomBar(
    tabs: Array<BottomBarTab>,
    currentTab: BottomBarTab = BottomBarTab.HOME,
    containerColor: Color = Colors.bottomBarContainer,
    onTabClick: (BottomBarTab) -> Unit = {}
) {
    BottomBarLayout(
        containerColor = containerColor,
    ) {
        tabs.forEach { tab ->
            val selected = (tab == currentTab)
            IconButton(onClick = { onTabClick(tab) }) {
                Icon(
                    painter = painterResource(tab.iconId),
                    tint = if (selected) {
                        Colors.bottomBarContent
                    } else {
                        Colors.inactive(Colors.bottomBarContent)
                    },
                    contentDescription = tab.title
                )
            }
        }
    }
}

@Composable
private fun BottomBarLayout(
    modifier: Modifier = Modifier,
    containerColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp + 24.dp)
            .padding(horizontal = 24.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp,
            ),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
            )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content()
            }
        }
    }
}
