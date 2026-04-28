package com.example.playlist_maker2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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