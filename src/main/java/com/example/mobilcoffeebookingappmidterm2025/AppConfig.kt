package com.example.mobilcoffeebookingappmidterm2025

/**
 * Safe runtime wrapper for BuildConfig.DEBUG that avoids compile-time unresolved
 * reference issues in preview or mixed classpath environments by using reflection.
 */
object AppConfig {
    val DEBUG: Boolean by lazy {
        try {
            val cls = Class.forName("com.example.mobilcoffeebookingappmidterm2025.BuildConfig")
            val field = cls.getField("DEBUG")
            field.getBoolean(null)
        } catch (t: Throwable) {
            // Fallback to false when BuildConfig is not available (e.g., previews)
            false
        }
    }
    
    val VERSION_NAME: String by lazy {
        try {
            val cls = Class.forName("com.example.mobilcoffeebookingappmidterm2025.BuildConfig")
            val field = cls.getField("VERSION_NAME")
            field.get(null)?.toString() ?: ""
        } catch (t: Throwable) {
            ""
        }
    }
}
