package com.example.playlist_maker2.domain.api

import com.example.playlist_maker2.domain.models.ThemeSettings

interface SettingsRepository {
   fun getThemeSettings(): ThemeSettings
   fun updateThemeSettings(settings: ThemeSettings)
}