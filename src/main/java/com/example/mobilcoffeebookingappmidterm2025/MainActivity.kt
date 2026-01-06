package com.example.mobilcoffeebookingappmidterm2025

import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.MobilCoffeeBookingAppMidterm2025Theme
import com.example.mobilcoffeebookingappmidterm2025.ui.home.HomeScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.auth.LoginScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.auth.SignUpScreen
import com.example.mobilcoffeebookingappmidterm2025.ui.splash.SplashScreen
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize repository (loads remember-me preference and configures auth state)
        MainRepository.init(applicationContext)
        enableEdgeToEdge()

        // Hide the navigation bar on launch and enable transient swipe-to-reveal behavior.
        // This makes the navigation bar hidden by default; users can swipe up to reveal it.
        WindowCompat.getInsetsController(window, window.decorView)?.let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            // Keep the theme selection across recompositions AND configuration changes (rotation)
            // using rememberSaveable so it survives activity recreation.
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            MobilCoffeeBookingAppMidterm2025Theme(darkTheme = isDarkTheme) {
                AppNavigation(isDarkTheme = isDarkTheme, onToggleTheme = { isDarkTheme = !isDarkTheme })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // App is backgrounded/stopping: sync current user state to Firestore
        // This ensures data is persisted even if app is killed by system
        android.util.Log.d("MainActivity", "App stopping - syncing to Firestore")
        MainRepository.syncToFirestore { success, error ->
            if (success) {
                android.util.Log.d("MainActivity", "Final sync successful")
            } else {
                android.util.Log.e("MainActivity", "Final sync failed: $error")
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Additional sync point when app goes to background
        android.util.Log.d("MainActivity", "App pausing - triggering sync")
        MainRepository.syncToFirestore()
    }
}

enum class AppScreen {
    SPLASH, LOGIN, SIGNUP, HOME
}

@Composable
fun AppNavigation(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    // Use rememberSaveable to preserve navigation state across configuration changes (rotation)
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.SPLASH) }
    val isLoggedIn by MainRepository.observeIsLoggedIn().collectAsState()

    // If user is already logged in and past splash, go directly to home
    if (isLoggedIn && currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.HOME) {
        currentScreen = AppScreen.HOME
    }
    
    // If user logs out, return to login screen
    if (!isLoggedIn && currentScreen == AppScreen.HOME) {
        currentScreen = AppScreen.LOGIN
    }

    when (currentScreen) {
        AppScreen.SPLASH -> {
            SplashScreen(
                onTimeout = {
                    // After splash, go to home if logged in, otherwise login
                    currentScreen = if (isLoggedIn) AppScreen.HOME else AppScreen.LOGIN
                }
            )
        }
        AppScreen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = { currentScreen = AppScreen.HOME },
                onNavigateToSignUp = { currentScreen = AppScreen.SIGNUP }
            )
        }
        AppScreen.SIGNUP -> {
            SignUpScreen(
                // After sign up we want the user to return to the Login screen (no auto-login)
                onSignUpSuccess = { currentScreen = AppScreen.LOGIN },
                onNavigateToLogin = { currentScreen = AppScreen.LOGIN }
            )
        }
        AppScreen.HOME -> {
            HomeScreen(isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobilCoffeeBookingAppMidterm2025Theme {
        Greeting("Android")
    }
}