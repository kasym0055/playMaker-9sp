package com.example.playlist_maker2.data

import com.example.playlist_maker2.domain.api.SettingsInteractor
import com.example.playlist_maker2.domain.api.SettingsRepository
import com.example.playlist_maker2.domain.models.ThemeSettings
import com.example.playlist_maker2.presentation.App

class SettingsInteractorImpl(
    private val repository: SettingsRepository,
    private val app: App
) : SettingsInteractor {

    override fun getThemeSettings(): ThemeSettings {
        return repository.getThemeSettings()
    }

    override fun updateThemeSettings(settings: ThemeSettings) {
        repository.updateThemeSettings(settings)
        app.switchTheme(settings.isDarkMode)
    }
}