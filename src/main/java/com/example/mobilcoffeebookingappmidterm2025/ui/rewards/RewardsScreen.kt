package com.example.mobilcoffeebookingappmidterm2025.ui.rewards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.model.PointReward
import com.example.mobilcoffeebookingappmidterm2025.ui.UIConfig
import com.example.mobilcoffeebookingappmidterm2025.ui.components.BottomBar
import com.example.mobilcoffeebookingappmidterm2025.ui.components.BottomBarTab
import com.example.mobilcoffeebookingappmidterm2025.ui.components.PointCard
import com.example.mobilcoffeebookingappmidterm2025.ui.components.RewardHistorySlot
import com.example.mobilcoffeebookingappmidterm2025.ui.components.StampCountCard
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onRedeemClick: () -> Unit = {},
    rewardsViewModel: RewardsViewModel = viewModel(factory = RewardsViewModel.Factory(MainRepository)),
    modifier: Modifier = Modifier
) {
    val stampCount by rewardsViewModel.stampCount.collectAsState()
    val points by rewardsViewModel.points.collectAsState()
    val history by rewardsViewModel.history.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Rewards",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomBar(
                tabs = BottomBarTab.values(),
                currentTab = BottomBarTab.REWARDS,
                onTabClick = { tab ->
                    when (tab) {
                        BottomBarTab.HOME -> onNavigateToHome()
                        BottomBarTab.REWARDS -> { /* Already on rewards */ }
                        BottomBarTab.ORDERS -> onNavigateToOrders()
                    }
                }
            )
        },
    ) { innerPadding ->
        val screenModifier = Modifier.padding(
            innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            innerPadding.calculateTopPadding(),
            innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            0.dp,
        )
        RewardsScreenContent(
            stampCount = stampCount,
            points = points,
            history = history,
            // Allow reset via tapping the stamp card when full
            onStampCardClick = { rewardsViewModel.resetStampCount() },
            onRedeemDrinksClick = onRedeemClick,
            modifier = screenModifier.padding(
                start = UIConfig.SCREEN_SIDE_PADDING,
                end = UIConfig.SCREEN_SIDE_PADDING,
                bottom = UIConfig.SCREEN_BOTTOM_PADDING
            )
        )
    }
}

@Composable
fun RewardsScreenContent(
    stampCount: Int,
    points: Int,
    history: List<PointReward>,
    onStampCardClick: () -> Unit,
    onRedeemDrinksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(15.dp),
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                StampCountCard(
                    stampCount = stampCount,
                    onClick = onStampCardClick,
                    containerColor = Colors.homeCardContainer,
                    contentColor = Colors.homeCardContent,
                    containerVariantColor = Colors.homeCardVariantContainer,
                    activeCupTint = Colors.homeActiveCupTint,
                )
                PointCard(
                    points = points,
                    containerColor = Colors.homeCardContainer,
                    contentColor = Colors.homeCardContent,
                    onRedeemClick = onRedeemDrinksClick
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "History Rewards",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        items(
            count = history.size,
            key = { index -> history[index].id },
        ) { index ->
            RewardHistorySlot(reward = history[history.lastIndex - index])
            if (index < history.lastIndex) {
                Spacer(modifier = Modifier.height(15.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
            }
        }
    }
}
