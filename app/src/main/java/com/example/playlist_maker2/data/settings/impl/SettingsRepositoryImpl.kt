package com.example.playlist_maker2.data.settings.impl

import android.content.SharedPreferences
import com.example.playlist_maker2.domain.settings.SettingsRepository
import com.example.playlist_maker2.domain.settings.model.ThemeSettings

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    companion object {
        private const val THEME_KEY = "key_for_dark_theme"
    }

    override fun getThemeSettings(): ThemeSettings {
        val isDarkMode = sharedPreferences.getBoolean(THEME_KEY, false)
        return ThemeSettings(isDarkMode = isDarkMode)
    }

    override fun updateThemeSettings(settings: ThemeSettings) {
        sharedPreferences.edit()
            .putBoolean(THEME_KEY, settings.isDarkMode)
            .apply()
    }
}