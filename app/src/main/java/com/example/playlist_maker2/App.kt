package com.example.playlist_maker2

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

const val PREFS_NAME = "DarkTheme"

class App : Application() {
    var darkTheme = false

    override fun onCreate() {
        super.onCreate()



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