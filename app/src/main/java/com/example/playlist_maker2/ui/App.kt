package com.example.playlist_maker2.ui

import android.app.Application
import com.example.playlist_maker2.di.dataModule
import com.example.playlist_maker2.di.interactorModule
import com.example.playlist_maker2.di.repositoryModule
import com.example.playlist_maker2.di.viewModelModule
import com.example.playlist_maker2.domain.settings.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

const val PREFS_NAME = "DarkTheme"

class App : Application() {
    var darkTheme = false

    override fun onCreate() {
        super.onCreate()

        val koinApplication = startKoin {
            androidContext(this@App)
            modules(dataModule, repositoryModule, interactorModule, viewModelModule)
        }

        val settingsRepository: SettingsRepository = koinApplication.koin.get()
        darkTheme = settingsRepository.getThemeSettings().isDarkMode
        settingsRepository.switchTheme(darkTheme)
    }
}
