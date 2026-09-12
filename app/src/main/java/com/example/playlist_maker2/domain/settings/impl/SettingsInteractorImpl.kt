package com.example.playlist_maker2.domain.settings.impl

import com.example.playlist_maker2.domain.settings.SettingsInteractor
import com.example.playlist_maker2.domain.settings.SettingsRepository
import com.example.playlist_maker2.domain.settings.model.ThemeSettings

class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {

    override fun getThemeSettings(): ThemeSettings {
        return repository.getThemeSettings()
    }

    override fun updateThemeSettings(settings: ThemeSettings) {
        repository.updateThemeSettings(settings)
    }
}
