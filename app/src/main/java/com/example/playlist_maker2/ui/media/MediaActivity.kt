package com.example.playlist_maker2.ui.media

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.example.playlist_maker2.R
import com.example.playlist_maker2.ui.media.view_model.MediaViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaActivity : AppCompatActivity() {
    private val viewModel: MediaViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media)
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val tabLayout = findViewById<TabLayout>(R.id.mediaTabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.mediaViewPager)

        viewPager.adapter = MediaPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getString(MediaPagerAdapter.getPageTitle(position))
        }.attach()

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
