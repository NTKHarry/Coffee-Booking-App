package com.example.mobilcoffeebookingappmidterm2025.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobilcoffeebookingappmidterm2025.R
import com.example.mobilcoffeebookingappmidterm2025.AppConfig
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.ui.CoffeeAvatar
import com.example.mobilcoffeebookingappmidterm2025.ui.UIConfig
import com.example.mobilcoffeebookingappmidterm2025.ui.components.BottomBar
import com.example.mobilcoffeebookingappmidterm2025.ui.components.BottomBarTab
import com.example.mobilcoffeebookingappmidterm2025.ui.components.StampCountCard
import com.example.mobilcoffeebookingappmidterm2025.ui.user.UserInfoScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.details.DetailsScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.cart.CartScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.orders.MyOrdersScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.redeem.RedeemScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.recommendation.RecommendationScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.rewards.RewardsScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors
// BuildConfig access is handled via AppConfig (reflection-safe wrapper)

// Screen state enum
private enum class ScreenState {
    HOME, DETAILS, CART, PAYMENT, SUCCESS, ORDERS, USER, REWARDS, REDEEM, RECOMMENDATION, AR_PREVIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // We use the singleton Repository
    // In a real app, this is done via DI (Hilt/Koin)
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(MainRepository)),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    // --- STATE FOR NAVIGATION ---
    // Use rememberSaveable to preserve state across configuration changes (rotation)
    var screenState by rememberSaveable { mutableStateOf(ScreenState.HOME) }
    var selectedProduct: String? by rememberSaveable { mutableStateOf(null) }
    
    when (screenState) {
        ScreenState.DETAILS -> {
            DetailsScreen(
                product = selectedProduct!!,
                onBackClick = {
                    // When user presses back from Details, return to HOME (not CART).
                    // CART is only shown when user explicitly adds to cart.
                    screenState = ScreenState.HOME
                    selectedProduct = null
                },
                onAddToCartNavigate = {
                    screenState = ScreenState.CART
                },
                onArPreviewClick = {
                    screenState = ScreenState.AR_PREVIEW
                }
            )
        }
        ScreenState.USER -> {
            UserInfoScreen(
                onBackClick = {
                    screenState = ScreenState.HOME
                },
                onLogout = {
                    // Logout is handled by repository which updates isLoggedIn
                    // AppNavigation will automatically navigate back to LOGIN screen
                }
            )
        }
        ScreenState.CART -> {
            CartScreen(
                onBackClick = {
                    screenState = ScreenState.HOME
                    selectedProduct = null
                },
                onTrackOrderClick = {
                    screenState = ScreenState.ORDERS
                },
                onProceedToPayment = {
                    screenState = ScreenState.PAYMENT
                }
            )
        }
        ScreenState.PAYMENT -> {
            // Payment screen gathers confirmation details, then proceeds to success
            com.example.mobilcoffeebookingappmidterm2025.ui.payment.PaymentScreen(
                onBackClick = { screenState = ScreenState.CART },
                onPaymentSuccess = { screenState = ScreenState.SUCCESS }
            )
        }
        ScreenState.SUCCESS -> {
            // Show the order success screen
            com.example.mobilcoffeebookingappmidterm2025.ui.cart.OrderSuccessScreen(
                onTrackOrderClick = { screenState = ScreenState.ORDERS },
                onReturnHomeClick = { screenState = ScreenState.HOME }
            )
        }
        ScreenState.ORDERS -> {
            MyOrdersScreen(
                onBottomBarClick = { tab ->
                    // Handle bottom bar tab clicks
                    when (tab) {
                        BottomBarTab.HOME -> screenState = ScreenState.HOME
                        BottomBarTab.ORDERS -> screenState = ScreenState.ORDERS
                        BottomBarTab.REWARDS -> screenState = ScreenState.REWARDS
                    }
                }
            )
        }
        ScreenState.REWARDS -> {
            RewardsScreen(
                onNavigateToHome = { screenState = ScreenState.HOME },
                onNavigateToOrders = { screenState = ScreenState.ORDERS },
                onRedeemClick = { screenState = ScreenState.REDEEM }
            )
        }
        ScreenState.REDEEM -> {
            RedeemScreen(
                onBackClick = {
                    screenState = ScreenState.REWARDS
                }
            )
        }
        ScreenState.RECOMMENDATION -> {
            RecommendationScreen(
                onBackClick = {
                    screenState = ScreenState.HOME
                },
                onDrinkSelected = { drinkName ->
                    selectedProduct = drinkName
                    screenState = ScreenState.DETAILS
                }
            )
        }
        ScreenState.AR_PREVIEW -> {
            com.example.mobilcoffeebookingappmidterm2025.ui.ar.ArPreviewScreen(
                productName = selectedProduct ?: "Coffee",
                onBackClick = {
                    screenState = ScreenState.DETAILS
                }
            )
        }
        ScreenState.HOME -> {
        // --- 1. COLLECT DATA FROM VIEWMODEL ---
        val fullName by homeViewModel.fullName.collectAsState()
        val stampCount by homeViewModel.stampCount.collectAsState()
        val categories = homeViewModel.categories
        val productsWithCategory = homeViewModel.productsWithCategory
        
        // Track selected category (use rememberSaveable to survive rotation)
        var selectedCategory by rememberSaveable { mutableStateOf(categories.firstOrNull() ?: "Coffee") }

        // --- 2. DRAW THE UI ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Text(
                            text = "Good morning",
                            style = MaterialTheme.typography.labelLarge.copy(color = Colors.onBackground2)
                        )
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 18.sp,
                                color = if (isDarkTheme) Colors.boldPrimary else MaterialTheme.colorScheme.onBackground,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                        )
                    }
                },
                actions = {
                    Row {
                        // Theme toggle button (light / dark)
                        IconButton(onClick = { onToggleTheme() }) {
                            val themeIcon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode
                            Icon(
                                imageVector = themeIcon,
                                contentDescription = "toggle theme",
                                tint = if (isDarkTheme) Colors.boldPrimary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = {
                            screenState = ScreenState.CART
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.cart),
                                contentDescription = "go to cart",
                                tint = if (isDarkTheme) Colors.boldPrimary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = { screenState = ScreenState.USER }) {
                            Icon(
                                painter = painterResource(R.drawable.person),
                                contentDescription = "go to profile",
                                tint = if (isDarkTheme) Colors.boldPrimary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier.padding(horizontal = UIConfig.TOP_BAR_SIDE_PADDING)
            )
        },
        bottomBar = {
            BottomBar(
                tabs = BottomBarTab.values(),
                currentTab = BottomBarTab.HOME,
                containerColor = Colors.homeCardVariantContainer,
                onTabClick = { tab ->
                    when (tab) {
                        BottomBarTab.HOME -> screenState = ScreenState.HOME
                        BottomBarTab.ORDERS -> screenState = ScreenState.ORDERS
                        BottomBarTab.REWARDS -> screenState = ScreenState.REWARDS
                    }
                }
            )
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(
            innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            innerPadding.calculateTopPadding() + 15.dp,
            innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            0.dp,
        )

        // Use extracted content composable so we can preview the screen easily
        HomeScreenContent(
            stampCount = stampCount,
            categories = categories,
            selectedCategory = selectedCategory,
            productsWithCategory = productsWithCategory,
            onCategorySelect = { selectedCategory = it },
            onStampCountClick = { homeViewModel.resetStampCount() },
            onCoffeeClick = { productName -> 
                selectedProduct = productName
                screenState = ScreenState.DETAILS
            },
            onRecommendationClick = {
                screenState = ScreenState.RECOMMENDATION
            },
            modifier = screenModifier
        )
    }
        }
    }
}

@Composable
fun HomeScreenContent(
    stampCount: Int,
    categories: List<String>,
    selectedCategory: String,
    productsWithCategory: List<MainRepository.Product>,
    onCategorySelect: (String) -> Unit,
    onStampCountClick: () -> Unit,
    onCoffeeClick: (String) -> Unit,
    onRecommendationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        // LOYALTY CARD
        StampCountCard(
            stampCount = stampCount,
            onClick = onStampCountClick,
            containerColor = Colors.homeCardContainer,
            contentColor = Colors.homeCardContent,
            containerVariantColor = Colors.homeCardVariantContainer,
            activeCupTint = Colors.homeActiveCupTint,
            modifier = Modifier.padding(horizontal = UIConfig.SCREEN_SIDE_PADDING)
        )
        
        // RECOMMENDATION BUTTON
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Colors.recommendationCardContainer
            ),
            onClick = onRecommendationClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UIConfig.SCREEN_SIDE_PADDING)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "💡",
                    style = MaterialTheme.typography.headlineMedium
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ask me",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Colors.recommendationCardContent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Get drink recommendations based on your mood",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Colors.recommendationCardContent.copy(alpha = 0.8f)
                        )
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.right_arrow),
                    contentDescription = "Go to recommendations",
                    tint = Colors.recommendationCardContent
                )
            }
        }

        // MAIN CONTENT AREA WITH CATEGORIES
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(Colors.homeCardContainer)
                .padding(
                    start = UIConfig.SCREEN_SIDE_PADDING,
                    end = UIConfig.SCREEN_SIDE_PADDING,
                    top = UIConfig.SCREEN_SIDE_PADDING,
                    bottom = UIConfig.SCREEN_BOTTOM_PADDING
                ),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    text = "Choose your drink",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Colors.homeCardContent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.padding(2.dp, 0.dp, 0.dp, 0.dp)
                )

                // CATEGORY MENU BAR (Fancy horizontal scrollable chips)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    items(
                        count = categories.size,
                        key = { idx -> categories[idx] }
                    ) { idx ->
                        val category = categories[idx]
                        val isSelected = category == selectedCategory
                        
                        // Category emoji mapping
                        val emoji = when(category) {
                            "Coffee" -> "☕"
                            "Smoothie" -> "🍹"
                            "Milk" -> "🥛"
                            "Alcoholic" -> "🍸"
                            else -> "🍵"
                        }
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelect(category) },
                            label = {
                                Text(
                                    text = "$emoji $category",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    )
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Colors.homeCardVariantContainer,
                                labelColor = Colors.homeCardVariantContent.copy(alpha = 0.6f),
                                selectedContainerColor = Colors.selectedCategoryChip,
                                selectedLabelColor = Colors.selectedCategoryChipContent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Colors.selectedCategoryChip else Colors.homeCardVariantContent.copy(alpha = 0.3f),
                                selectedBorderColor = Colors.selectedCategoryChip,
                                borderWidth = 1.5.dp,
                                selectedBorderWidth = 1.5.dp
                            ),
                            elevation = FilterChipDefaults.filterChipElevation(
                                elevation = if (isSelected) 4.dp else 2.dp
                            )
                        )
                    }
                }

                // PRODUCTS GRID WITH ANIMATED TRANSITION
                val filteredProducts = productsWithCategory.filter { it.category == selectedCategory }
                
                AnimatedContent(
                    targetState = selectedCategory,
                    transitionSpec = {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -fullWidth },
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                            )
                    },
                    label = "category_transition"
                ) { category ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            count = filteredProducts.size,
                            key = { idx -> filteredProducts[idx].name }
                        ) { idx ->
                            val product = filteredProducts[idx]
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Colors.homeCardVariantContainer
                                ),
                                onClick = { onCoffeeClick(product.name) },
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp, horizontal = 8.dp)
                                ) {
                                    CoffeeAvatar(
                                        coffee = product.name, 
                                        width = 110.dp,     
                                        height = 120.dp
                                    )

                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = Colors.homeCardVariantContent,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    
                                    Text(
                                        text = "$${String.format("%.2f", product.basePrice)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Colors.homeCardVariantContent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    Surface {
        // Provide sample data for preview
        HomeScreenContent(
            stampCount = 4,
            categories = listOf("Coffee", "Smoothie", "Milk", "Alcoholic"),
            selectedCategory = "Coffee",
            productsWithCategory = listOf(
                MainRepository.Product("Americano", "Coffee", 2.50),
                MainRepository.Product("Cappuccino", "Coffee", 3.50),
                MainRepository.Product("Latte", "Coffee", 3.50),
                MainRepository.Product("Flat White", "Coffee", 3.75)
            ),
            onCategorySelect = {},
            onStampCountClick = {},
            onCoffeeClick = {},
            onRecommendationClick = {},
            modifier = Modifier.fillMaxSize().padding(16.dp)
        )
    }
}


