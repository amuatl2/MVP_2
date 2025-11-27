package com.example.mvp.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocalizationManager {
    private var currentLocale: Locale = Locale.getDefault()
    
    fun setLocale(context: Context, languageCode: String) {
        currentLocale = Locale(languageCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(currentLocale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    fun getCurrentLocale(): Locale = currentLocale
    
    fun getSupportedLanguages(): List<Language> {
        return listOf(
            Language("en", "English"),
            Language("es", "Español"),
            Language("fr", "Français"),
            Language("de", "Deutsch"),
            Language("zh", "中文"),
            Language("ja", "日本語")
        )
    }
    
    data class Language(
        val code: String,
        val name: String
    )
    
    // String resources would be in res/values-{language}/
    // For now, this provides the infrastructure
    fun getString(context: Context, key: String, default: String = key): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) {
            context.getString(resId)
        } else {
            default
        }
    }
}

