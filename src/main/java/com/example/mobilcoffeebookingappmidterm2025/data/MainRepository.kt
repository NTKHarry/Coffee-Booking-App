package com.example.mobilcoffeebookingappmidterm2025.data

import com.example.mobilcoffeebookingappmidterm2025.R

import com.example.mobilcoffeebookingappmidterm2025.model.CartItem
import com.example.mobilcoffeebookingappmidterm2025.model.Order
import com.example.mobilcoffeebookingappmidterm2025.model.PointReward
import com.example.mobilcoffeebookingappmidterm2025.model.ProductOption
import com.example.mobilcoffeebookingappmidterm2025.model.Redeemable
import com.example.mobilcoffeebookingappmidterm2025.model.SizeType
import com.example.mobilcoffeebookingappmidterm2025.model.ShotType
import com.example.mobilcoffeebookingappmidterm2025.model.VoucherOwned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
// Firebase Storage removed — this app uses only Auth and Firestore

// This is a Singleton - there's only ONE instance shared across the entire app.
// In a real app, you would use Hilt/Koin for DI. For this demo, we use Kotlin 'object'.
object
MainRepository {

    // Application context set during init(context)
    private var appContext: Context? = null

    // --- MOCK DATABASE (RAM) ---
    // 1. User Info (Dummy) - cleared for new users (no mock data)
    private val _fullName = MutableStateFlow("")
    private val _address = MutableStateFlow("")

    // 2. Loyalty Card - New users start with 0 stamps
    private val _stampCount = MutableStateFlow(0)
    
    // 3. Points and Rewards - New users start with 0 points, empty history
    private val _points = MutableStateFlow(0)
    private val _pointsHistory = MutableStateFlow<List<PointReward>>(emptyList())
    
    // 4. Redeemables - Drinks that can be redeemed with points
    // Points required = 1000 * base price (Americano: 2.50 -> 2500 pts, Latte: 3.50 -> 3500 pts)
    private val _redeemables = MutableStateFlow<List<Redeemable>>(
        listOf(
            Redeemable("r1", "Americano", "31 Dec 2025", 2500),
            Redeemable("r2", "Latte", "31 Dec 2025", 3500),
        )
    )
    
    // 5. Vouchers - Can be purchased with points (300 pts each)
    private val _availableVouchers = MutableStateFlow<List<com.example.mobilcoffeebookingappmidterm2025.model.Coupon>>(
        listOf(
            com.example.mobilcoffeebookingappmidterm2025.model.Coupon(id = "v1", label = "5% off", percentOff = 5),
            com.example.mobilcoffeebookingappmidterm2025.model.Coupon(id = "v2", label = "10% off", percentOff = 10),
        )
    )
    private val VOUCHER_COST = 300 // Points cost per voucher
    
    // 6. Owned Vouchers - Track user's vouchers with quantities
    private val _ownedVouchers = MutableStateFlow<List<VoucherOwned>>(emptyList())

    // 7. Product Categories & Items (only drinks with available images)
    data class Product(
        val name: String,
        val category: String,
        val basePrice: Double = 3.50
    )
    
    private val products = listOf(
        // Coffee (12 items with images)
        Product("Americano", "Coffee", 2.50),
        Product("Cappuccino", "Coffee", 3.50),
        Product("Latte", "Coffee", 3.50),
        Product("Flat White", "Coffee", 3.75),
        Product("Macchiato", "Coffee", 3.00),
        Product("Mocha", "Coffee", 4.00),
        Product("Cortado", "Coffee", 3.25),
        Product("Doppio", "Coffee", 2.75),
        Product("Affogato", "Coffee", 4.50),
        Product("Irish Coffee", "Coffee", 5.50),
        Product("Nitro Cold Brew", "Coffee", 4.25),
        Product("Bulletproof Coffee", "Coffee", 4.75),
        Product("Butter Coffee", "Coffee", 4.50),
        Product("Chemex", "Coffee", 3.80),
        Product("French Press", "Coffee", 3.60),
        Product("Moka Pot", "Coffee", 3.40),

        // Milk Drinks (8 items with images)
        Product("Chocolate Milk", "Milk", 3.00),
        Product("Oat Milk", "Milk", 2.80),
        Product("Almond Milk", "Milk", 2.80),
        Product("Soy Milk", "Milk", 2.80),
        Product("Coconut Milk", "Milk", 3.00),
        Product("Malted Milkshake", "Milk", 4.50),
        Product("Banana Milkshake", "Milk", 4.25),
        Product("Steamer", "Milk", 2.50),
        Product("Hot Milk", "Milk", 2.00),
        Product("Maple Milk", "Milk", 3.20),

        // Smoothies (11 items with images)
        Product("Mango Smoothie", "Smoothie", 5.00),
        Product("Strawberry Smoothie", "Smoothie", 4.75),
        Product("Berry Blast Smoothie", "Smoothie", 5.25),
        Product("Tropical Smoothie", "Smoothie", 5.50),
        Product("Protein Smoothie", "Smoothie", 6.00),
        Product("Mixed Berry Smoothie", "Smoothie", 5.00),
        Product("Blueberry Smoothie", "Smoothie", 4.75),
        Product("Raspberry Smoothie", "Smoothie", 4.75),
        Product("Peanut Butter Banana Smoothie", "Smoothie", 5.50),
        Product("Pomegranate Smoothie", "Smoothie", 5.25),
        Product("Coconut Smoothie", "Smoothie", 5.00),
        Product("Oat Smoothie", "Smoothie", 4.50),

        // Alcoholic (9 items with images)
        Product("Beer Lager", "Alcoholic", 5.00),
        Product("Stout", "Alcoholic", 5.50),
        Product("Old Fashioned", "Alcoholic", 8.00),
        Product("Manhattan", "Alcoholic", 8.50),
        Product("Rum Punch", "Alcoholic", 7.00),
        Product("Tequila Sunrise", "Alcoholic", 7.50),
        Product("Bellini", "Alcoholic", 7.00)
    )

    // 8. Cart - List of CartItems
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())

    // 9. Orders
    private val _ongoingOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _historyOrders = MutableStateFlow<List<Order>>(emptyList())

    // 10. User info (mock data)
    // New users should start with empty contact info; values will be populated from
    // Firebase Authentication (displayName/email) or Firestore when available.
    private val _phoneNumber = MutableStateFlow("+1 (555) 123-4567")
    private val _email = MutableStateFlow("")
    private val _deliveryLocation = MutableStateFlow("123 Coffee Street, Bean City")
    // Use existing drawable as avatar placeholder
    private val _avatarResId = MutableStateFlow(R.drawable.person)
    // Optional remote photo URL stored in Firestore
    private val _photoUrl = MutableStateFlow<String?>(null)

    // 7. Firebase Authentication
    private val auth = FirebaseAuth.getInstance()
    private var _isLoggedIn = MutableStateFlow(false)

    // Remember-me preference (stored in SharedPreferences)
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_REMEMBER_ME = "remember_me"
    private var rememberMe: Boolean = false
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        rememberMe = prefs?.getBoolean(KEY_REMEMBER_ME, false) ?: false
        // Keep application context for helpers that need to read assets or resources
        appContext = context.applicationContext
        
        android.util.Log.d("MainRepository", "Init - RememberMe from SharedPrefs: $rememberMe")

        // Check if user is signed in with Firebase
        val current = auth.currentUser
        android.util.Log.d("MainRepository", "Init - Current Firebase user: ${current?.email}")
        
        if (current != null) {
            // User is signed in with Firebase, check Firestore for remember_login status
            val uid = current.uid
            android.util.Log.d("MainRepository", "Checking Firestore for remember_login status for user: $uid")
            
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { userDoc ->
                    val rememberLogin = userDoc.getBoolean("remember_login") ?: false
                    android.util.Log.d("MainRepository", "Firestore remember_login: $rememberLogin")
                    
                    if (rememberLogin) {
                        // User chose to be remembered - auto-login
                        android.util.Log.d("MainRepository", "Auto-login: User remembered (from Firestore)")
                        rememberMe = true
                        prefs?.edit()?.putBoolean(KEY_REMEMBER_ME, true)?.apply()
                        _email.value = current.email ?: ""
                        _fullName.value = current.displayName ?: "User"
                        _isLoggedIn.value = true
                        // Load user data from Firestore
                        loadFromFirestore()
                    } else {
                        // User didn't opt to be remembered -> sign out
                        android.util.Log.d("MainRepository", "No auto-login: remember_login is false in Firestore")
                        rememberMe = false
                        prefs?.edit()?.putBoolean(KEY_REMEMBER_ME, false)?.apply()
                        auth.signOut()
                        _isLoggedIn.value = false
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("MainRepository", "Failed to check Firestore remember_login", e)
                    // Fallback to SharedPreferences behavior
                    if (rememberMe) {
                        android.util.Log.d("MainRepository", "Fallback: Auto-login from SharedPrefs")
                        _email.value = current.email ?: ""
                        _fullName.value = current.displayName ?: "User"
                        _isLoggedIn.value = true
                        loadFromFirestore()
                    } else {
                        android.util.Log.d("MainRepository", "Fallback: No auto-login, signing out")
                        auth.signOut()
                        _isLoggedIn.value = false
                    }
                }
        } else {
            // No Firebase user signed in
            android.util.Log.d("MainRepository", "No Firebase user, not logged in")
            _isLoggedIn.value = false
        }
    }

    /**
     * Read a small secret file from the assets folder. Returns null if not found or on error.
     * Example: place your GROQ API key in app/src/main/assets/groq_api_key.txt
     */
    fun readSecretFromAssets(filename: String): String? {
        val ctx = appContext ?: return null
        return try {
            ctx.assets.open(filename).bufferedReader().use { it.readText().trim() }
        } catch (e: Exception) {
            android.util.Log.w("MainRepository", "Failed to read asset $filename: ${e.message}")
            null
        }
    }

    /** Convenience accessor for the GROQ API key stored in assets/groq_api_key.txt */
    fun getGroqApiKey(): String? = readSecretFromAssets("groq_api_key.txt")
    
    // 8. Coupons (mock)
    private val _coupons = MutableStateFlow<List<com.example.mobilcoffeebookingappmidterm2025.model.Coupon>>(
        listOf(
            com.example.mobilcoffeebookingappmidterm2025.model.Coupon(id = "c1", label = "5% off", percentOff = 5),
            com.example.mobilcoffeebookingappmidterm2025.model.Coupon(id = "c2", label = "10% off", percentOff = 10),
        )
    )

    // --- EXPOSE DATA TO VIEWMODELS ---
    fun observeFullName(): StateFlow<String> = _fullName.asStateFlow()
    fun observeStampCount(): StateFlow<Int> = _stampCount.asStateFlow()
    fun observePoints(): StateFlow<Int> = _points.asStateFlow()
    fun observePointsHistory(): StateFlow<List<PointReward>> = _pointsHistory.asStateFlow()
    fun observeRedeemables(): StateFlow<List<Redeemable>> = _redeemables.asStateFlow()
    fun observeAvailableVouchers(): StateFlow<List<com.example.mobilcoffeebookingappmidterm2025.model.Coupon>> = _availableVouchers.asStateFlow()
    fun observeOwnedVouchers(): StateFlow<List<VoucherOwned>> = _ownedVouchers.asStateFlow()
    fun observeCart(): StateFlow<List<CartItem>> = _cart.asStateFlow()
    fun observeOngoingOrders(): StateFlow<List<Order>> = _ongoingOrders.asStateFlow()
    fun observeHistoryOrders(): StateFlow<List<Order>> = _historyOrders.asStateFlow()
    fun observePhoneNumber(): StateFlow<String> = _phoneNumber.asStateFlow()
    fun observeEmail(): StateFlow<String> = _email.asStateFlow()
    fun observeDeliveryLocation(): StateFlow<String> = _deliveryLocation.asStateFlow()
    fun observeAvatarResId(): StateFlow<Int> = _avatarResId.asStateFlow()
    fun observePhotoUrl(): StateFlow<String?> = _photoUrl.asStateFlow()
    fun observeCoupons(): StateFlow<List<com.example.mobilcoffeebookingappmidterm2025.model.Coupon>> = _coupons.asStateFlow()
    
    // Product access functions
    fun getProducts(): List<String> = products.map { it.name }
    fun getProductsWithCategory(): List<Product> = products
    fun getCategories(): List<String> = products.map { it.category }.distinct().sorted()
    fun getProductsByCategory(category: String): List<Product> = products.filter { it.category == category }
    fun getProductPrice(productName: String): Double = products.find { it.name == productName }?.basePrice ?: 3.50

    // --- ACTIONS (Simulate saving to Firebase) ---
    suspend fun resetStampCount() {
        // Simulate network delay
        delay(500)
        _stampCount.value = 0
        android.util.Log.d("MainRepository", "Stamp count reset to 0")
        // Sync to Firestore to persist the reset
        syncToFirestore()
    }

    fun addToCart(product: String, option: ProductOption) {
        // If an identical item (same product and options except quantity) exists in the cart,
        // merge by increasing the quantity and updating the price. Otherwise add a new item.
        _cart.update { currentCart ->
            // Find existing item with same product and same option fields (excluding quantity)
            val existingIndex = currentCart.indexOfFirst { existing ->
                existing.product == product &&
                        existing.option.shot == option.shot &&
                        existing.option.temperature == option.temperature &&
                        existing.option.size == option.size &&
                        existing.option.ice == option.ice
            }

            if (existingIndex >= 0) {
                val existing = currentCart[existingIndex]
                val newQuantity = existing.option.quantity + option.quantity
                val updatedOption = existing.option.copy(quantity = newQuantity)
                val updatedItem = existing.copy(
                    option = updatedOption,
                    price = getPrice(product, updatedOption)
                )
                val mutable = currentCart.toMutableList()
                mutable[existingIndex] = updatedItem
                val updatedCart = mutable.toList()
                android.util.Log.d("MainRepository", "Cart merged: item updated with new quantity=$newQuantity")
                updatedCart
            } else {
                val price = getPrice(product, option)
                val newItem = CartItem(
                    id = UUID.randomUUID().toString(),
                    product = product,
                    price = price,
                    option = option
                )
                val updatedCart = currentCart + newItem
                android.util.Log.d("MainRepository", "Cart updated: ${updatedCart.size} items")
                updatedCart
            }
        }
        // Trigger sync AFTER cart update completes
        android.util.Log.d("MainRepository", "Cart update complete, triggering Firestore sync")
        syncToFirestore()
    }

    fun removeFromCart(itemId: String): Boolean {
        val item = _cart.value.find { it.id == itemId } ?: return false
        _cart.update { it - item }
        android.util.Log.d("MainRepository", "Cart item removed: ${item.product}")
        // Trigger sync AFTER cart update completes
        syncToFirestore()
        return true
    }

    // --- PRODUCT PRICING ---
    fun getPrice(product: String, option: ProductOption): Double {
        // Base prices for each coffee
        val basePrice = when (product.lowercase()) {
            "americano" -> 2.50
            "cappuccino" -> 3.00
            "latte" -> 3.50
            "flat white" -> 3.25
            else -> 3.00
        }

        // Size multiplier
        val sizeMultiplier = when (option.size) {
            SizeType.SMALL -> 0.8
            SizeType.MEDIUM -> 1.0
            SizeType.LARGE -> 1.3
        }

        // Shot extra cost
        val shotExtra = when (option.shot) {
            ShotType.SINGLE -> 0.0
            ShotType.DOUBLE -> 0.50
        }

        // Calculate total: (base * size + shot) * quantity
        return (basePrice * sizeMultiplier + shotExtra) * option.quantity
    }
    
    // --- POINTS & REDEMPTION LOGIC ---
    
    /**
     * Redeem a drink using points and add it to ongoing orders
     * Deducts points based on formula: pointsRequired = 1000 * base price
     * Creates a free order (price = 0.0) and adds to ongoing orders
     * Returns true if redemption successful, false if insufficient points
     */
    fun redeemDrink(redeemableId: String): Boolean {
        val redeemable = _redeemables.value.find { it.id == redeemableId } ?: return false
        
        // Check if user has enough points
        if (_points.value < redeemable.pointsRequired) {
            android.util.Log.w("MainRepository", "Insufficient points for ${redeemable.product}: need ${redeemable.pointsRequired}, have ${_points.value}")
            return false
        }
        
        // Deduct points
        _points.value -= redeemable.pointsRequired
        
        // Add to points history as a redemption (negative points)
        val currentDateTime = getCurrentDateTime()
        val newHistory = PointReward(
            id = UUID.randomUUID().toString(),
            product = "Redeemed ${redeemable.product}",
            datetime = currentDateTime,
            points = -redeemable.pointsRequired
        )
        _pointsHistory.update { it + newHistory }
        
        // Create a free order for the redeemed drink with default options
        val defaultOption = ProductOption(
            quantity = 1,
            shot = ShotType.SINGLE,
            temperature = com.example.mobilcoffeebookingappmidterm2025.model.TemperatureType.HOT,
            size = SizeType.MEDIUM,
            ice = com.example.mobilcoffeebookingappmidterm2025.model.IceType.FULL
        )
        
        val redeemedOrder = Order(
            id = UUID.randomUUID().toString(),
            product = redeemable.product,
            datetime = currentDateTime,
            price = 0.0, // Free - redeemed with points
            address = _deliveryLocation.value,
            option = defaultOption,
            paymentMethod = "Points Redemption",
            couponPercent = 0
        )
        
        // Add to ongoing orders
        _ongoingOrders.update { it + redeemedOrder }
        
        android.util.Log.d("MainRepository", "Successfully redeemed ${redeemable.product} for ${redeemable.pointsRequired} pts. Remaining: ${_points.value}")
        
        // Trigger sync to Firestore
        syncToFirestore()
        
        return true
    }
    
    /**
     * Purchase a voucher using points
     * Each voucher costs 300 points
     * Returns true if purchase successful, false if insufficient points
     */
    fun purchaseVoucher(voucherId: String): Boolean {
        val voucher = _availableVouchers.value.find { it.id == voucherId } ?: return false
        
        // Check if user has enough points
        if (_points.value < VOUCHER_COST) {
            android.util.Log.w("MainRepository", "Insufficient points for voucher: need $VOUCHER_COST, have ${_points.value}")
            return false
        }
        
        // Deduct points
        _points.value -= VOUCHER_COST
        
        // Add or update voucher in owned vouchers list
        _ownedVouchers.update { currentVouchers ->
            // First, remove any zero-quantity vouchers to prevent duplicates
            val cleanedVouchers = currentVouchers.filter { it.quantity > 0 }
            
            val existingVoucher = cleanedVouchers.find { it.voucherId == voucher.id }
            if (existingVoucher != null) {
                // Increment quantity of existing voucher
                cleanedVouchers.map { owned ->
                    if (owned.voucherId == voucher.id) {
                        owned.copy(quantity = owned.quantity + 1)
                    } else {
                        owned
                    }
                }
            } else {
                // Add new voucher with quantity 1
                cleanedVouchers + VoucherOwned(
                    voucherId = voucher.id,
                    label = voucher.label,
                    percentOff = voucher.percentOff,
                    quantity = 1
                )
            }
        }
        
        // Add voucher to coupons if not already present (for backward compatibility)
        _coupons.update { currentCoupons ->
            if (currentCoupons.any { it.id == voucher.id }) {
                currentCoupons
            } else {
                currentCoupons + voucher
            }
        }
        
        // Add to points history
        val currentDateTime = getCurrentDateTime()
        val newHistory = PointReward(
            id = UUID.randomUUID().toString(),
            product = "Voucher ${voucher.label}",
            datetime = currentDateTime,
            points = -VOUCHER_COST
        )
        _pointsHistory.update { it + newHistory }
        
        android.util.Log.d("MainRepository", "Successfully purchased voucher ${voucher.label} for $VOUCHER_COST pts. Remaining: ${_points.value}")
        
        // Trigger sync to Firestore
        syncToFirestore()
        
        return true
    }
    
    /**
     * Use a voucher - decrements its quantity
     * Returns true if successful, false if voucher not found or quantity is 0
     */
    fun useVoucher(voucherId: String): Boolean {
        val voucher = _ownedVouchers.value.find { it.voucherId == voucherId } ?: return false
        
        if (voucher.quantity <= 0) {
            return false
        }
        
        _ownedVouchers.update { currentVouchers ->
            currentVouchers.map { owned ->
                if (owned.voucherId == voucherId) {
                    owned.copy(quantity = owned.quantity - 1)
                } else {
                    owned
                }
            }.filter { it.quantity > 0 } // Remove vouchers with 0 quantity
        }
        
        android.util.Log.d("MainRepository", "Used voucher ${voucher.label}, remaining: ${voucher.quantity - 1}")
        
        // Trigger sync to Firestore
        syncToFirestore()
        
        return true
    }
    
    /**
     * Add points based on purchase
     * Formula: points earned = price * 100
     */
    private fun addPoints(price: Double, productName: String) {
        val pointsEarned = (price * 100).toInt()
        _points.value += pointsEarned
        
        // Add to points history
        val currentDateTime = getCurrentDateTime()
        val newHistory = PointReward(
            id = UUID.randomUUID().toString(),
            product = productName,
            datetime = currentDateTime,
            points = pointsEarned
        )
        _pointsHistory.update { it + newHistory }
        
        android.util.Log.d("MainRepository", "Earned $pointsEarned pts from $productName purchase. Total: ${_points.value}")
    }

    // --- ORDER MANAGEMENT ---
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Checkout the current cart. Optionally provide an address override (temporary),
     * the discount percent (coupon) and the selected payment method.
     * 
     * Also increments loyalty card stamps: +1 stamp per checkout (regardless of cart quantity).
     * Stamps are capped at 8 and require an explicit user reset to go back to 0.
     */
    fun checkOut(addressOverride: String? = null, discountPercent: Int = 0, paymentMethod: String? = null): Boolean {
        if (_cart.value.isEmpty()) return false

        val addressToUse = addressOverride ?: _address.value
        val currentDateTime = getCurrentDateTime()

        val orders = _cart.value.map { cartItem ->
            val discountedPrice = cartItem.price * (1.0 - discountPercent / 100.0)
            // Round to 2 decimal places without converting to locale-formatted string
            val rounded = kotlin.math.round(discountedPrice * 100.0) / 100.0
            
            // Add points for this purchase (points = price * 100)
            addPoints(rounded, cartItem.product)
            
            Order(
                id = UUID.randomUUID().toString(),
                product = cartItem.product,
                datetime = currentDateTime,
                price = rounded,
                address = addressToUse,
                option = cartItem.option,
                paymentMethod = paymentMethod,
                couponPercent = discountPercent
            )
        }

        // Award exactly one loyalty stamp for this checkout (regardless of how many items were in the cart)
        _stampCount.update { currentStamps ->
            if (currentStamps >= 8) {
                android.util.Log.d("MainRepository", "Loyalty card already at max (8) - no additional stamp awarded")
                8
            } else {
                val newStamps = (currentStamps + 1).coerceAtMost(8)
                android.util.Log.d("MainRepository", "Stamp added: $currentStamps -> $newStamps (1 per checkout)")
                newStamps
            }
        }

        // Add all orders to ongoing
        _ongoingOrders.update { it + orders }

        // Clear cart
        _cart.value = emptyList()

        android.util.Log.d("MainRepository", "Checkout successful: ${orders.size} orders created (discount=$discountPercent%), stamps: ${_stampCount.value}")
        
        // Trigger sync to Firestore after checkout (critical - must persist)
        syncToFirestore()
        
        return true
    }

    fun moveToHistory(orderId: String) {
        val order = _ongoingOrders.value.find { it.id == orderId } ?: return
        // Update status to COMPLETED
        val completedOrder = order.copy(status = com.example.mobilcoffeebookingappmidterm2025.model.OrderStatus.COMPLETED)
        _ongoingOrders.update { it - order }
        _historyOrders.update { it + completedOrder }
        android.util.Log.d("MainRepository", "Order moved to history: ${order.id}")
        // Trigger sync to Firestore
        syncToFirestore()
    }

    fun moveToOngoing(orderId: String) {
        val order = _historyOrders.value.find { it.id == orderId } ?: return
        // Update status to ONGOING
        val ongoingOrder = order.copy(status = com.example.mobilcoffeebookingappmidterm2025.model.OrderStatus.ONGOING)
        _historyOrders.update { it - order }
        _ongoingOrders.update { it + ongoingOrder }
        android.util.Log.d("MainRepository", "Order moved to ongoing: ${order.id}")
        // Trigger sync to Firestore
        syncToFirestore()
    }

    fun removeHistoryOrder(orderId: String) {
        val order = _historyOrders.value.find { it.id == orderId } ?: return
        _historyOrders.update { it - order }
        android.util.Log.d("MainRepository", "Order removed from history: ${order.id}")
        // Trigger sync to Firestore
        syncToFirestore()
    }

    // Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Load user data, cart, and orders from Firestore on app start.
     * This restores the user's state from the cloud database.
     */
    fun loadFromFirestore(onComplete: ((Boolean, String?) -> Unit)? = null) {
        val user = auth.currentUser ?: run {
            onComplete?.invoke(false, "No authenticated user")
            return
        }

        val uid = user.uid
        android.util.Log.d("MainRepository", "Loading data from Firestore for user: $uid")

        // Load user document
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    // Restore user info from Firestore document
                    userDoc.getString("fullName")?.let { _fullName.value = it }
                    userDoc.getString("phoneNumber")?.let { _phoneNumber.value = it }
                    userDoc.getString("deliveryLocation")?.let { _deliveryLocation.value = it }
                    // Load photoUrl if present
                    userDoc.getString("photoUrl")?.let { _photoUrl.value = it }
                    userDoc.getLong("stamps")?.toInt()?.let { _stampCount.value = it }
                    // Load points
                    userDoc.getLong("points")?.toInt()?.let { _points.value = it }
                    android.util.Log.d("MainRepository", "User doc loaded successfully")
                } else {
                    // No Firestore user document exists yet. Clear any local mock values and
                    // initialize sensible defaults for a new user. Keep display name from
                    // FirebaseAuth if available (so we show the signed-in user's name).
                    _fullName.value = user.displayName ?: ""
                    _email.value = user.email ?: ""
                    _phoneNumber.value = "+1 (555) 123-4567"
                    _deliveryLocation.value = "123 Coffee Street, Bean City"
                    _photoUrl.value = null
                    _stampCount.value = 0
                    _points.value = 0
                    _pointsHistory.value = emptyList()
                    _ownedVouchers.value = emptyList()
                    _cart.value = emptyList()
                    _ongoingOrders.value = emptyList()
                    _historyOrders.value = emptyList()
                    android.util.Log.d("MainRepository", "No user doc found - initialized defaults for new user")
                    
                    // Sync the initial state to Firestore to create the user document
                    syncToFirestore { success, error ->
                        if (success) {
                            android.util.Log.d("MainRepository", "Initial user data synced to Firestore")
                        } else {
                            android.util.Log.e("MainRepository", "Failed to sync initial user data: $error")
                        }
                    }
                    
                    // Still call onComplete for the load operation
                    onComplete?.invoke(true, null)
                    return@addOnSuccessListener
                }
                
                // Load points history
                firestore.collection("users").document(uid)
                    .collection("pointsHistory")
                    .get()
                    .addOnSuccessListener { historySnapshot ->
                        val history = historySnapshot.documents.mapNotNull { doc ->
                            try {
                                PointReward(
                                    id = doc.getString("id") ?: return@mapNotNull null,
                                    product = doc.getString("product") ?: return@mapNotNull null,
                                    datetime = doc.getString("datetime") ?: return@mapNotNull null,
                                    points = (doc.get("points") as? Number)?.toInt() ?: return@mapNotNull null
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("MainRepository", "Error parsing points history", e)
                                null
                            }
                        }
                        _pointsHistory.value = history
                        android.util.Log.d("MainRepository", "Points history loaded: ${history.size} items")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("MainRepository", "Failed to load points history", e)
                    }
                
                // Load owned vouchers
                firestore.collection("users").document(uid)
                    .collection("ownedVouchers")
                    .get()
                    .addOnSuccessListener { vouchersSnapshot ->
                        val vouchers = vouchersSnapshot.documents.mapNotNull { doc ->
                            try {
                                VoucherOwned(
                                    voucherId = doc.getString("voucherId") ?: return@mapNotNull null,
                                    label = doc.getString("label") ?: return@mapNotNull null,
                                    percentOff = (doc.get("percentOff") as? Number)?.toInt() ?: return@mapNotNull null,
                                    quantity = (doc.get("quantity") as? Number)?.toInt() ?: return@mapNotNull null
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("MainRepository", "Error parsing owned voucher", e)
                                null
                            }
                        }.filter { it.quantity > 0 } // Filter out zero-quantity vouchers when loading
                        _ownedVouchers.value = vouchers
                        android.util.Log.d("MainRepository", "Owned vouchers loaded: ${vouchers.size} items")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("MainRepository", "Failed to load owned vouchers", e)
                    }

                // Load cart
                firestore.collection("users").document(uid)
                    .collection("cart").document("cart_doc")
                    .get()
                    .addOnSuccessListener { cartDoc ->
                        if (cartDoc.exists()) {
                            val items = cartDoc.get("items") as? List<Map<String, Any>> ?: emptyList()
                            val cartItems = items.mapNotNull { itemMap ->
                                try {
                                    val optionMap = itemMap["option"] as? Map<String, Any> ?: return@mapNotNull null
                                    CartItem(
                                        id = itemMap["id"] as? String ?: return@mapNotNull null,
                                        product = itemMap["product"] as? String ?: return@mapNotNull null,
                                        price = (itemMap["price"] as? Number)?.toDouble() ?: return@mapNotNull null,
                                        option = ProductOption(
                                            quantity = (optionMap["quantity"] as? Number)?.toInt() ?: 1,
                                            shot = ShotType.valueOf(optionMap["shot"] as? String ?: "SINGLE"),
                                            temperature = com.example.mobilcoffeebookingappmidterm2025.model.TemperatureType.valueOf(
                                                optionMap["temperature"] as? String ?: "HOT"
                                            ),
                                            size = SizeType.valueOf(optionMap["size"] as? String ?: "MEDIUM"),
                                            ice = com.example.mobilcoffeebookingappmidterm2025.model.IceType.valueOf(
                                                optionMap["ice"] as? String ?: "FULL"
                                            )
                                        )
                                    )
                                } catch (e: Exception) {
                                    android.util.Log.e("MainRepository", "Error parsing cart item", e)
                                    null
                                }
                            }
                            _cart.value = cartItems
                            android.util.Log.d("MainRepository", "Cart loaded with ${cartItems.size} items")
                        }

                        // Load orders
                        firestore.collection("users").document(uid)
                            .collection("orders")
                            .get()
                            .addOnSuccessListener { ordersSnapshot ->
                                val orders = ordersSnapshot.documents.mapNotNull { doc ->
                                    try {
                                        val optionMap = doc.get("option") as? Map<String, Any> ?: return@mapNotNull null
                                        val statusString = doc.getString("status") ?: "ONGOING"
                                        val status = try {
                                            com.example.mobilcoffeebookingappmidterm2025.model.OrderStatus.valueOf(statusString)
                                        } catch (e: Exception) {
                                            com.example.mobilcoffeebookingappmidterm2025.model.OrderStatus.ONGOING
                                        }
                                        
                                        Order(
                                            id = doc.getString("id") ?: return@mapNotNull null,
                                            product = doc.getString("product") ?: return@mapNotNull null,
                                            datetime = doc.getString("datetime") ?: return@mapNotNull null,
                                            price = (doc.get("price") as? Number)?.toDouble() ?: return@mapNotNull null,
                                            address = doc.getString("address") ?: return@mapNotNull null,
                                            option = ProductOption(
                                                quantity = (optionMap["quantity"] as? Number)?.toInt() ?: 1,
                                                shot = ShotType.valueOf(optionMap["shot"] as? String ?: "SINGLE"),
                                                temperature = com.example.mobilcoffeebookingappmidterm2025.model.TemperatureType.valueOf(
                                                    optionMap["temperature"] as? String ?: "HOT"
                                                ),
                                                size = SizeType.valueOf(optionMap["size"] as? String ?: "MEDIUM"),
                                                ice = com.example.mobilcoffeebookingappmidterm2025.model.IceType.valueOf(
                                                    optionMap["ice"] as? String ?: "FULL"
                                                )
                                            ),
                                            paymentMethod = doc.getString("paymentMethod"),
                                            couponPercent = (doc.get("couponPercent") as? Number)?.toInt() ?: 0,
                                            status = status
                                        )
                                    } catch (e: Exception) {
                                        android.util.Log.e("MainRepository", "Error parsing order", e)
                                        null
                                    }
                                }
                                
                                // Separate orders by status
                                val ongoing = orders.filter { it.status == com.example.mobilcoffeebookingappmidterm2025.model.OrderStatus.ONGOING }
                                val completed = orders.filter { it.status == com.example.mobilcoffeebookingappmidterm2025.model.OrderStatus.COMPLETED }
                                
                                _ongoingOrders.value = ongoing
                                _historyOrders.value = completed
                                android.util.Log.d("MainRepository", "Orders loaded: ${ongoing.size} ongoing, ${completed.size} completed")
                                onComplete?.invoke(true, null)
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("MainRepository", "Failed to load orders", e)
                                onComplete?.invoke(false, e.message)
                            }
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("MainRepository", "Failed to load cart", e)
                        onComplete?.invoke(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("MainRepository", "Failed to load user doc", e)
                onComplete?.invoke(false, e.message)
            }
    }

    /**
     * Sync current repository user state to Firestore under users/{uid} and subcollections.
     * This is intentionally tolerant (uses SetOptions.merge) and will overwrite per-session state.
     */
    fun syncToFirestore(onComplete: ((Boolean, String?) -> Unit)? = null) {
        val user = auth.currentUser ?: run {
            android.util.Log.w("MainRepository", "syncToFirestore: No authenticated user - skipping sync")
            onComplete?.invoke(false, "No authenticated user")
            return
        }

        val uid = user.uid
        
        android.util.Log.d("MainRepository", "========== STARTING FIRESTORE SYNC ==========")
        android.util.Log.d("MainRepository", "User: $uid")
        android.util.Log.d("MainRepository", "Stamps: ${_stampCount.value}")
        android.util.Log.d("MainRepository", "Points: ${_points.value}")
        android.util.Log.d("MainRepository", "Cart items: ${_cart.value.size}")
        android.util.Log.d("MainRepository", "Points history: ${_pointsHistory.value.size}")
        android.util.Log.d("MainRepository", "Ongoing orders: ${_ongoingOrders.value.size}")
        android.util.Log.d("MainRepository", "History orders: ${_historyOrders.value.size}")

        // Prepare basic user doc map
        val userDoc = hashMapOf<String, Any>(
            "id" to uid,
            "email" to (_email.value ?: ""),
            "fullName" to (_fullName.value ?: ""),
            // include remote photo url when present
            "photoUrl" to (_photoUrl.value ?: ""),
            "phoneNumber" to (_phoneNumber.value ?: ""),
            "deliveryLocation" to (_deliveryLocation.value ?: ""),
            "stamps" to _stampCount.value,
            "points" to _points.value,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        // Write user doc (merge so we don't clobber server-managed fields)
        firestore.collection("users").document(uid)
            .set(userDoc, SetOptions.merge())
            .addOnSuccessListener {
                android.util.Log.d("MainRepository", "User doc synced successfully")
                
                // Sync points history
                val historyRef = firestore.collection("users").document(uid).collection("pointsHistory")
                val historyTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
                
                _pointsHistory.value.forEach { reward ->
                    val rewardMap = hashMapOf<String, Any>(
                        "id" to reward.id,
                        "product" to reward.product,
                        "datetime" to reward.datetime,
                        "points" to reward.points
                    )
                    val task = historyRef.document(reward.id).set(rewardMap, SetOptions.merge())
                    historyTasks.add(task)
                }
                
                // Sync owned vouchers (only sync vouchers with quantity > 0)
                val vouchersRef = firestore.collection("users").document(uid).collection("ownedVouchers")
                val voucherTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
                
                // Only sync vouchers that have quantity > 0
                _ownedVouchers.value.filter { it.quantity > 0 }.forEach { voucher ->
                    val voucherMap = hashMapOf<String, Any>(
                        "voucherId" to voucher.voucherId,
                        "label" to voucher.label,
                        "percentOff" to voucher.percentOff,
                        "quantity" to voucher.quantity
                    )
                    val task = vouchersRef.document(voucher.voucherId).set(voucherMap, SetOptions.merge())
                    voucherTasks.add(task)
                }
                
                // Delete vouchers with zero quantity from Firestore
                _ownedVouchers.value.filter { it.quantity == 0 }.forEach { voucher ->
                    val task = vouchersRef.document(voucher.voucherId).delete()
                    voucherTasks.add(task)
                }
                
                // Sync cart as subdoc
                val cartDoc = hashMapOf<String, Any>(
                    "id" to "cart_doc",
                    "items" to _cart.value.map { item ->
                        mapOf(
                            "id" to item.id,
                            "product" to item.product,
                            "price" to item.price,
                            "option" to mapOf(
                                "size" to item.option.size.name,
                                "shot" to item.option.shot.name,
                                "temperature" to item.option.temperature.name,
                                "ice" to item.option.ice.name,
                                "quantity" to item.option.quantity
                            )
                        )
                    },
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                android.util.Log.d("MainRepository", "Syncing cart with ${_cart.value.size} items")
                
                firestore.collection("users").document(uid)
                    .collection("cart").document("cart_doc")
                    .set(cartDoc, SetOptions.merge())
                    .addOnSuccessListener {
                        android.util.Log.d("MainRepository", "Cart synced successfully")
                        // Sync orders (ongoing + history) into users/{uid}/orders/{orderId}
                        val allOrders = _ongoingOrders.value + _historyOrders.value
                        android.util.Log.d("MainRepository", "Syncing ${allOrders.size} orders")
                        
                        if (allOrders.isEmpty()) {
                            android.util.Log.d("MainRepository", "No orders to sync, completing")
                            onComplete?.invoke(true, null)
                            return@addOnSuccessListener
                        }
                        
                        val syncTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()

                        allOrders.forEach { order ->
                            val orderMap = orderToMap(order)
                            val task = firestore.collection("users").document(uid)
                                .collection("orders").document(order.id)
                                .set(orderMap, SetOptions.merge())
                            syncTasks.add(task)
                        }

                        // Wait for all order sync tasks using Tasks.whenAllComplete
                        com.google.android.gms.tasks.Tasks.whenAllComplete(syncTasks)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    android.util.Log.d("MainRepository", "========== FIRESTORE SYNC COMPLETE ==========")
                                    android.util.Log.d("MainRepository", "All ${syncTasks.size} orders synced successfully")
                                    onComplete?.invoke(true, null)
                                } else {
                                    android.util.Log.e("MainRepository", "Some orders failed to sync", task.exception)
                                    onComplete?.invoke(false, task.exception?.message)
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("MainRepository", "Cart sync failed: ${e.message}", e)
                        onComplete?.invoke(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("MainRepository", "User doc sync failed: ${e.message}", e)
                onComplete?.invoke(false, e.message)
            }
    }

    private fun orderToMap(order: Order): Map<String, Any> {
        return mapOf(
            "id" to order.id,
            "product" to order.product,
            "datetime" to order.datetime,
            "price" to order.price,
            "address" to order.address,
            "paymentMethod" to (order.paymentMethod ?: ""),
            "couponPercent" to (order.couponPercent ?: 0),
            "status" to order.status.name,
            "option" to mapOf(
                "size" to order.option.size.name,
                "shot" to order.option.shot.name,
                "temperature" to order.option.temperature.name,
                "ice" to order.option.ice.name,
                "quantity" to order.option.quantity
            )
        )
    }

    // --- FIREBASE AUTH FUNCTIONS ---
    fun observeIsLoggedIn(): StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * Set the remember-me preference. If true, the app will keep the user signed-in across restarts.
     * This saves to both SharedPreferences (local) and Firestore (cloud) for persistence.
     */
    fun setRememberMe(context: Context, remember: Boolean) {
        prefs = prefs ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs?.edit()
        editor?.putBoolean(KEY_REMEMBER_ME, remember)
        editor?.apply()
        rememberMe = remember
        
        android.util.Log.d("MainRepository", "RememberMe set to: $remember (SharedPrefs)")
        
        // Also save to Firestore for persistence across app reinstalls/devices
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            firestore.collection("users").document(uid)
                .set(
                    hashMapOf("remember_login" to remember),
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    android.util.Log.d("MainRepository", "RememberMe saved to Firestore: $remember")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("MainRepository", "Failed to save RememberMe to Firestore", e)
                }
        } else {
            android.util.Log.w("MainRepository", "No user logged in, cannot save RememberMe to Firestore")
        }
    }

    fun isRemembered(): Boolean = rememberMe

    /**
     * Register a new user with Firebase Authentication
     * @param email User's email address
     * @param username Display name for the user
     * @param password User's password
     * @param onSuccess Callback on successful registration
     * @param onFailure Callback with error message on failure
     */
    fun register(
        email: String,
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update user profile with display name
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                // After creating the account, sign out so the user must login explicitly.
                                // This prevents automatic login immediately after signup.
                                auth.signOut()
                                // Do NOT set _isLoggedIn here. Caller should navigate back to Login screen.
                                onSuccess()
                            } else {
                                onFailure(updateTask.exception?.message ?: "Failed to update profile")
                            }
                        }
                } else {
                    onFailure(task.exception?.message ?: "Registration failed")
                }
            }
    }

    /**
     * Login with Firebase Authentication
     * @param email User's email address
     * @param password User's password
     * @param rememberMe Whether to remember the login for auto-login on next app start
     * @param onSuccess Callback on successful login
     * @param onFailure Callback with error message on failure
     */
    fun login(
        email: String,
        password: String,
        rememberMe: Boolean = false,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        _email.value = user.email ?: ""
                        _fullName.value = user.displayName ?: "User"
                        _isLoggedIn.value = true
                        
                        // Create/update user document in Firestore with remember_login
                        val uid = user.uid
                        val userDoc = hashMapOf<String, Any>(
                            "id" to uid,
                            "email" to (user.email ?: ""),
                            "fullName" to (user.displayName ?: "User"),
                            "remember_login" to rememberMe,
                            "updatedAt" to com.google.firebase.Timestamp.now()
                        )
                        
                        android.util.Log.d("MainRepository", "========== LOGIN FLOW START ==========")
                        android.util.Log.d("MainRepository", "Login: User authenticated - UID: $uid")
                        android.util.Log.d("MainRepository", "Login: Email: ${user.email}")
                        android.util.Log.d("MainRepository", "Login: RememberMe parameter: $rememberMe")
                        android.util.Log.d("MainRepository", "Login: User doc to write: $userDoc")
                        android.util.Log.d("MainRepository", "Login: Firestore path: users/$uid")
                        android.util.Log.d("MainRepository", "Login: Starting Firestore write...")
                        
                        firestore.collection("users").document(uid)
                            .set(userDoc, SetOptions.merge())
                            .addOnSuccessListener {
                                android.util.Log.d("MainRepository", "✓ SUCCESS: Firestore write completed successfully!")
                                android.util.Log.d("MainRepository", "✓ Firestore document users/$uid updated with remember_login=$rememberMe")
                                
                                // Persist rememberMe locally as well
                                prefs = prefs ?: appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                val editor = prefs?.edit()?.putBoolean(KEY_REMEMBER_ME, rememberMe)
                                editor?.apply()
                                this@MainRepository.rememberMe = rememberMe
                                android.util.Log.d("MainRepository", "✓ SharedPreferences updated with remember_me=$rememberMe")

                                // Load rest of user data from Firestore after successful login
                                loadFromFirestore()

                                android.util.Log.d("MainRepository", "========== LOGIN FLOW END (SUCCESS) ==========")
                                // Notify caller that login flow (including user doc write) completed
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("MainRepository", "✗ FAILURE: Firestore write FAILED!")
                                android.util.Log.e("MainRepository", "✗ Error writing to users/$uid", e)
                                android.util.Log.e("MainRepository", "✗ Error message: ${e.message}")
                                android.util.Log.e("MainRepository", "✗ Error type: ${e.javaClass.simpleName}")
                                
                                // Still load data even if user doc creation fails
                                loadFromFirestore()

                                // Persist local preference even if Firestore write failed
                                prefs = prefs ?: appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                val editor = prefs?.edit()?.putBoolean(KEY_REMEMBER_ME, rememberMe)
                                editor?.apply()
                                this@MainRepository.rememberMe = rememberMe
                                android.util.Log.d("MainRepository", "✓ SharedPreferences updated anyway with remember_me=$rememberMe")

                                android.util.Log.d("MainRepository", "========== LOGIN FLOW END (FIRESTORE FAILED) ==========")
                                // Notify caller that login succeeded (we don't block login on Firestore writes)
                                onSuccess()
                            }
                    } else {
                        // No user found after sign-in - treat as failure
                        onFailure("Login failed: user not found")
                        return@addOnCompleteListener
                    }
                } else {
                    onFailure(task.exception?.message ?: "Login failed")
                }
            }
    }

    /**
     * Logout the current user and clear all session data
     */
    fun logout() {
        android.util.Log.d("MainRepository", "Logging out user")
        
        // Clear remember_login in Firestore BEFORE signing out (while we still have auth)
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            firestore.collection("users").document(uid)
                .set(
                    hashMapOf("remember_login" to false),
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    android.util.Log.d("MainRepository", "remember_login cleared in Firestore")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("MainRepository", "Failed to clear remember_login in Firestore", e)
                }
        }
        
        // Clear remember-me preference in SharedPreferences
        prefs?.edit()?.putBoolean(KEY_REMEMBER_ME, false)?.apply()
        rememberMe = false
        
        // Clear all local state
        _cart.value = emptyList()
        _ongoingOrders.value = emptyList()
        _historyOrders.value = emptyList()
        _stampCount.value = 0
        
        // Sign out from Firebase
        auth.signOut()
        _isLoggedIn.value = false
        
        android.util.Log.d("MainRepository", "Logout complete - RememberMe cleared (local and Firestore)")
    }

    /**
     * Get the current user's display name
     */
    fun getCurrentUsername(): String? {
        return auth.currentUser?.displayName
    }

    /**
     * Update user information (name, phone, location) and sync to Firestore
     */
    fun updateUserInfo(
        fullName: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        deliveryLocation: String? = null,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        // Update local state
        fullName?.let { _fullName.value = it }
        phoneNumber?.let { _phoneNumber.value = it }
        email?.let { _email.value = it }
        deliveryLocation?.let { _deliveryLocation.value = it }
        
        // Sync to Firestore
        val user = auth.currentUser
        if (user == null) {
            onFailure("No authenticated user")
            return
        }
        
        val uid = user.uid
        val updates = hashMapOf<String, Any>()
        fullName?.let { updates["fullName"] = it }
        phoneNumber?.let { updates["phoneNumber"] = it }
        email?.let { updates["email"] = it }
        deliveryLocation?.let { updates["deliveryLocation"] = it }
        updates["updatedAt"] = com.google.firebase.Timestamp.now()
        
        firestore.collection("users").document(uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                android.util.Log.d("MainRepository", "User info updated successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("MainRepository", "Failed to update user info", e)
                onFailure(e.message ?: "Update failed")
            }
    }
}