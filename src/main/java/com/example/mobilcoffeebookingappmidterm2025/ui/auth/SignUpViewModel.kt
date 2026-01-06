package com.example.mobilcoffeebookingappmidterm2025.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignUpViewModel(private val repository: MainRepository) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _confirmVisible = MutableStateFlow(false)
    val confirmVisible: StateFlow<Boolean> = _confirmVisible.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setEmail(value: String) {
        _email.value = value
        _errorMessage.value = ""
    }

    fun setUsername(value: String) {
        _username.value = value
        _errorMessage.value = ""
    }

    fun setPassword(value: String) {
        _password.value = value
        _errorMessage.value = ""
    }

    fun setConfirmPassword(value: String) {
        _confirmPassword.value = value
        _errorMessage.value = ""
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun toggleConfirmVisibility() {
        _confirmVisible.value = !_confirmVisible.value
    }

    fun signUp() {
        val emailValue = _email.value.trim()
        val usernameValue = _username.value.trim()
        val passwordValue = _password.value
        val confirmPasswordValue = _confirmPassword.value

        // Validation
        when {
            emailValue.isEmpty() -> {
                _errorMessage.value = "Please enter your email"
                return
            }
            !emailValue.contains("@") -> {
                _errorMessage.value = "Please enter a valid email"
                return
            }
            usernameValue.isEmpty() -> {
                _errorMessage.value = "Please enter a username"
                return
            }
            passwordValue.isEmpty() -> {
                _errorMessage.value = "Please enter a password"
                return
            }
            passwordValue.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                return
            }
            passwordValue != confirmPasswordValue -> {
                _errorMessage.value = "Passwords do not match"
                return
            }
        }

        // Attempt registration with Firebase (auto-login happens inside Firebase auth)
        _isLoading.value = true
        repository.register(
            email = emailValue,
            username = usernameValue,
            password = passwordValue,
            onSuccess = {
                _isLoading.value = false
                _signUpSuccess.value = true
                _errorMessage.value = ""
            },
            onFailure = { error ->
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
                    return SignUpViewModel(repository) as T
                }
            }
    }
}
