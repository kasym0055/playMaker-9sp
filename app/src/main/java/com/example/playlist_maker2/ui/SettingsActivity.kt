package com.example.playlist_maker2.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.playlist_maker2.creator.Creator
import com.example.playlist_maker2.R
import com.example.playlist_maker2.domain.settings.SettingsInteractor
import com.example.playlist_maker2.domain.sharing.SharingInteractor
import com.example.playlist_maker2.domain.settings.model.ThemeSettings
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var settingsInteractor: SettingsInteractor
    private lateinit var sharingInteractor: SharingInteractor
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val arrBack = findViewById<LinearLayout>(R.id.arr_back)
        val shareApp = findViewById<LinearLayout>(R.id.share_app)
        val chatSupport = findViewById<LinearLayout>(R.id.chatSupport)
        val userAgreement = findViewById<LinearLayout>(R.id.userAgreement)
        val switchDarkTheme = findViewById<SwitchMaterial>(R.id.switchDarkTheme)

        settingsInteractor = Creator.provideSettingsInteractor(this)
        sharingInteractor = Creator.provideSharingInteractor(this)

        val currentSettings = settingsInteractor.getThemeSettings()
        switchDarkTheme.isChecked = currentSettings.isDarkMode

        arrBack.setOnClickListener {
            finish()
        }

        switchDarkTheme.setOnCheckedChangeListener { switcher, checked  ->
            settingsInteractor.updateThemeSettings(ThemeSettings(isDarkMode = checked))
        }

        shareApp.setOnClickListener {
            sharingInteractor.shareApp()

        }

        chatSupport.setOnClickListener {
            sharingInteractor.openSupport()
        }

        userAgreement.setOnClickListener {
            sharingInteractor.openTerms()
        }
    }
}