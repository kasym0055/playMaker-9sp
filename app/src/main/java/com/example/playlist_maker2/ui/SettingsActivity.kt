package com.example.playlist_maker2.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.playlist_maker2.creator.Creator
import com.example.playlist_maker2.R
import com.example.playlist_maker2.domain.settings.SettingsInteractor
import com.example.playlist_maker2.domain.sharing.SharingInteractor
import com.example.playlist_maker2.domain.settings.model.ThemeSettings
import com.example.playlist_maker2.ui.search.view_model.SettingsViewModel
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.getViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val arrBack = findViewById<LinearLayout>(R.id.arr_back)
        val shareApp = findViewById<LinearLayout>(R.id.share_app)
        val chatSupport = findViewById<LinearLayout>(R.id.chatSupport)
        val userAgreement = findViewById<LinearLayout>(R.id.userAgreement)
        val switchDarkTheme = findViewById<SwitchMaterial>(R.id.switchDarkTheme)

        viewModel.observeThemeSettings().observe(this) { themeSettings ->
            switchDarkTheme.isChecked = themeSettings.isDarkMode
        }

        arrBack.setOnClickListener {
            finish()
        }

        switchDarkTheme.setOnCheckedChangeListener { switcher, checked  ->
            viewModel.updateTheme(checked)
        }

        shareApp.setOnClickListener {
            viewModel.shareApp()

        }

        chatSupport.setOnClickListener {
            viewModel.openSupport()
        }

        userAgreement.setOnClickListener {
            viewModel.openTerms()
        }
    }
}