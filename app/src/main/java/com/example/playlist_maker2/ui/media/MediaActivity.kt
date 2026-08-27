package com.example.playlist_maker2.ui.media

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.playlist_maker2.R
import com.example.playlist_maker2.ui.media.models.MediaState
import com.example.playlist_maker2.ui.media.view_model.MediaViewModel

class MediaActivity : AppCompatActivity() {
    private val viewModel: MediaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media)

        viewModel.observeState().observe(this){ state->
            render(state)
        }
    }
    private fun render(state: MediaState){
        when(state){
            is MediaState.Loading -> showLoading()
            is MediaState.Empty -> showEmpty()
            is MediaState.Content -> showContent()
        }
    }
    private fun showLoading(){}
    private fun showEmpty(){}
    private fun showContent(){}
}