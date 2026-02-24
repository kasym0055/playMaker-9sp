package com.example.playlist_maker2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val searchBtn = findViewById<LinearLayout>(R.id.search_btn)
        val mediaBtn =findViewById<LinearLayout>(R.id.media_btn)
        val settingBtn= findViewById<LinearLayout>(R.id.settings_btn)


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