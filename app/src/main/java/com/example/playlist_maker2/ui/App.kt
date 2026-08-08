package com.example.playlist_maker2.ui

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

const val PREFS_NAME = "DarkTheme"
private const val THEME_KEY = "key_for_dark_theme"

class App : Application() {
    var darkTheme = false

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        darkTheme = sharedPreferences.getBoolean(THEME_KEY,false)

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