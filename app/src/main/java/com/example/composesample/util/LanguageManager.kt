package com.example.composesample.util

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

object LanguageManager {
    private const val LANGUAGE_PREFS = "language_prefs"
    private const val IS_KOREAN_KEY = "is_korean"

    fun saveLanguagePreference(context: Context, isKorean: Boolean) {
        val sharedPrefs = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit { putBoolean(IS_KOREAN_KEY, isKorean) }
    }

    fun getLanguagePreference(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(IS_KOREAN_KEY, true)
    }

    fun createLocalizedContext(context: Context): Context {
        val isKorean = getLanguagePreference(context)
        val locale = if (isKorean) Locale.KOREAN else Locale.ENGLISH
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getCurrentLanguageDisplayName(context: Context): String {
        val isKorean = getLanguagePreference(context)
        return if (isKorean) "한국어" else "English"
    }
}