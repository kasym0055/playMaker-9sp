package com.example.playlist_maker2.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.playlist_maker2.R
import com.example.playlist_maker2.ui.settings.view_model.SettingsViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

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
