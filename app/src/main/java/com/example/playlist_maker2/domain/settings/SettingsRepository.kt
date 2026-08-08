package com.example.playlist_maker2.domain.settings

import com.example.playlist_maker2.domain.settings.model.ThemeSettings

interface SettingsRepository {
   fun getThemeSettings(): ThemeSettings
   fun updateThemeSettings(settings: ThemeSettings)
}