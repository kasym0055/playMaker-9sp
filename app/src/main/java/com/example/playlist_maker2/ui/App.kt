package com.example.playlist_maker2.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlist_maker2.di.dataModule
import com.example.playlist_maker2.di.interactorModule
import com.example.playlist_maker2.di.repositoryModule
import com.example.playlist_maker2.di.viewModelModule
import com.example.playlist_maker2.domain.settings.SettingsInteractor
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

        val settingsInteractor: SettingsInteractor = koinApplication.koin.get()
        darkTheme = settingsInteractor.getThemeSettings().isDarkMode
        switchTheme(darkTheme)
    }
    fun switchTheme(darkThemeEnabled: Boolean){
        darkTheme=darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled){
                AppCompatDelegate.MODE_NIGHT_YES
            }else{
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
