package com.example.playlist_maker2

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val arrBack = findViewById<LinearLayout>(R.id.arr_back)
        val shareApp = findViewById<LinearLayout>(R.id.share_app)
        val chatSupport = findViewById<LinearLayout>(R.id.chatSupport)
        val userAgreement = findViewById<LinearLayout>(R.id.userAgreement)
        val switchDarkTheme = findViewById<SwitchMaterial>(R.id.switchDarkTheme)
        
        arrBack.setOnClickListener {
            finish()
        }
        
        switchDarkTheme.setOnCheckedChangeListener { switcher, checked  ->
            (applicationContext as App).switchTheme(checked )
         }
        
        shareApp.setOnClickListener {
            val message = getString(R.string.share_message)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type="text/plain"
                putExtra(Intent.EXTRA_TEXT,message)

            }
            startActivity(Intent.createChooser(shareIntent,"share via"))

        }

        chatSupport.setOnClickListener {
            val message = getString(R.string.head_message)
            val subject = getString(R.string.body_message)
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            shareIntent.data = Uri.parse("mailto:")
            shareIntent.putExtra(Intent.EXTRA_EMAIL,arrayOf(getString(R.string.your_email)))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,subject)
            shareIntent.putExtra(Intent.EXTRA_TEXT,message)
            startActivity(shareIntent)
        }

        userAgreement.setOnClickListener {
            val url = getString(R.string.practicum_offer_url)
            val shareIntent = Intent(Intent.ACTION_VIEW)
            shareIntent.data= Uri.parse(url)
            startActivity(shareIntent)
        }
    }
}