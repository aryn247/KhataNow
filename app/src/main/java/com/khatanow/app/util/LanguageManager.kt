package com.khatanow.app.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en-IN", "English"),
    HINDI("hi-IN", "हिंदी (Hindi)")
}

object LanguageManager {
    private const val PREF_LANG = "pref_app_language"
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("khata_now_prefs", Context.MODE_PRIVATE)
        val savedCode = prefs.getString(PREF_LANG, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        val lang = AppLanguage.values().find { it.code == savedCode } ?: AppLanguage.ENGLISH
        _currentLanguage.value = lang
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences("khata_now_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANG, language.code).apply()
        _currentLanguage.value = language
    }
}
