package com.example.mobilcoffeebookingappmidterm2025.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(private val repository: MainRepository) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Remember me checkbox state - stored in ViewModel to survive recompositions
    private val _rememberMe = MutableStateFlow(repository.isRemembered())
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    fun setEmail(value: String) {
        _email.value = value
        _errorMessage.value = ""
    }

    fun setPassword(value: String) {
        _password.value = value
        _errorMessage.value = ""
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }
    
    fun setRememberMe(value: Boolean) {
        android.util.Log.d("LoginViewModel", "setRememberMe called with: $value")
        _rememberMe.value = value
    }

    fun login() {
        val emailValue = _email.value.trim()
        val passwordValue = _password.value
        val rememberMeValue = _rememberMe.value

        android.util.Log.d("LoginViewModel", "========== LOGIN ATTEMPT ==========")
        android.util.Log.d("LoginViewModel", "Email: $emailValue")
        android.util.Log.d("LoginViewModel", "RememberMe from state: $rememberMeValue")

        // Validation
        when {
            emailValue.isEmpty() -> {
                _errorMessage.value = "Please enter your email"
                android.util.Log.d("LoginViewModel", "Validation failed: Email empty")
                return
            }
            passwordValue.isEmpty() -> {
                _errorMessage.value = "Please enter your password"
                android.util.Log.d("LoginViewModel", "Validation failed: Password empty")
                return
            }
            !emailValue.contains("@") -> {
                _errorMessage.value = "Please enter a valid email"
                android.util.Log.d("LoginViewModel", "Validation failed: Invalid email format")
                return
            }
        }

        android.util.Log.d("LoginViewModel", "Validation passed, calling repository.login with rememberMe=$rememberMeValue")
        
        // Attempt login with Firebase
        _isLoading.value = true
        repository.login(
            email = emailValue,
            password = passwordValue,
            rememberMe = rememberMeValue,
            onSuccess = {
                android.util.Log.d("LoginViewModel", "✓ Repository login SUCCESS callback received")
                _isLoading.value = false
                _loginSuccess.value = true
                _errorMessage.value = ""
            },
            onFailure = { error ->
                android.util.Log.e("LoginViewModel", "✗ Repository login FAILURE callback received: $error")
                _isLoading.value = false
                _errorMessage.value = error
            }
        )
    }

    companion object {
        fun Factory(repository: MainRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return LoginViewModel(repository) as T
                }
            }
    }
}
