package com.example.mobilcoffeebookingappmidterm2025.ui.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class RecommendationViewModel(
    private val repository: MainRepository
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<String>>(emptyList())
    val recommendations: StateFlow<List<String>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // Get all available products for reference
    private val availableProducts = repository.getProducts()

    /**
     * Call Groq API to get drink recommendations based on user mood.
     * The LLM should return exactly 5 drink names from our menu.
     */
    suspend fun getRecommendations(mood: String) {
        _isLoading.value = true
        _errorMessage.value = ""
        
        viewModelScope.launch {
            try {
                val drinkNames = callGroqApi(mood)
                _recommendations.value = drinkNames
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get recommendations: ${e.message}"
                _recommendations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Make HTTP request to Groq API.
     * Using llama-3.1-8b-instant model for fast responses.
     */
    private suspend fun callGroqApi(mood: String): List<String> = withContext(Dispatchers.IO) {
        // Load API key from assets (app/src/main/assets/groq_api_key.txt)
        val apiKey = repository.getGroqApiKey()
            ?: throw Exception("GROQ API key not found in assets/groq_api_key.txt")
        val apiUrl = "https://api.groq.com/openai/v1/chat/completions"
        
        // Build prompt with available drinks
        val drinksList = availableProducts.joinToString(", ")
        val systemPrompt = """
            You are a drink recommendation assistant for a coffee shop.
            Available drinks: $drinksList

            Based on the user's mood, recommend drinks from the available list that match the mood.
            Return ONLY the drink names, one per line, nothing else. The number of recommendations is up to the assistant; do not pad responses with commentary.
            Make sure the names exactly match the available drinks list when possible.
        """.trimIndent()
        
        val userPrompt = "My mood: $mood"
        
        // Build JSON request
        val jsonRequest = JSONObject().apply {
            put("model", "llama-3.1-8b-instant") // Fast Groq model
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            })
            put("temperature", 1.0)
            put("max_tokens", 1024)
            put("top_p", 1.0)
            put("stream", false) // We'll use non-streaming for simpler parsing
        }
        
        // Make HTTP request
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            
            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonRequest.toString())
                writer.flush()
            }
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                
                // Parse response
                val jsonResponse = JSONObject(response)
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val message = choices.getJSONObject(0).getJSONObject("message")
                    val content = message.getString("content")
                    
                    // Parse drink names (one per line). Accept any number returned by the model,
                    // but cap to a safe maximum to avoid runaway outputs.
                    val rawNames = content.trim()
                        .split("\n")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    // Validate that returned drinks exist in our menu
                    val validDrinks = rawNames.filter { drinkName ->
                        availableProducts.any { it.equals(drinkName, ignoreCase = true) }
                    }

                    // Cap number of recommendations to 20 for safety
                    val capped = if (validDrinks.size > 20) validDrinks.take(20) else validDrinks

                    if (capped.isEmpty()) {
                        throw Exception("No valid drinks found in LLM response")
                    }

                    return@withContext capped
                } else {
                    throw Exception("No recommendations returned from API")
                }
            } else {
                val errorStream = connection.errorStream
                val errorMessage = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else {
                    "HTTP error code: $responseCode"
                }
                throw Exception(errorMessage)
            }
        } finally {
            connection.disconnect()
        }
    }

    class Factory(private val repository: MainRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
                return RecommendationViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
