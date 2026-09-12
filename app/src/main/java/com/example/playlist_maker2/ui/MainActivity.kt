package com.example.playlist_maker2.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.playlist_maker2.R
import com.example.playlist_maker2.ui.media.MediaActivity
import com.example.playlist_maker2.ui.search.SearchActivity
import com.example.playlist_maker2.ui.settings.SettingsActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }
        val searchBtn = findViewById<MaterialButton>(R.id.search_btn)
        val mediaBtn =findViewById<MaterialButton>(R.id.media_btn)
        val settingBtn= findViewById<MaterialButton>(R.id.settings_btn)

        searchBtn.setOnClickListener  {
            val searchIntent= Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
        }

        mediaBtn.setOnClickListener {
            val mediaIntent= Intent(this, MediaActivity::class.java)
            startActivity(mediaIntent)
        }

        settingBtn.setOnClickListener {
            val settingsIntent= Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
    }

}
