package com.example.playlist_maker2.domain.settings.impl

import com.example.playlist_maker2.domain.settings.SettingsInteractor
import com.example.playlist_maker2.domain.settings.SettingsRepository
import com.example.playlist_maker2.domain.settings.model.ThemeSettings
import com.example.playlist_maker2.ui.App

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