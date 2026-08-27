package com.example.playlist_maker2.ui.player

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.playlist_maker2.R
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.player.models.PlayerState
import com.example.playlist_maker2.ui.player.view_model.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var playButton: ImageButton
    private lateinit var trackLength: TextView

    private val viewModel: PlayerViewModel by viewModels {
        PlayerViewModel.Companion.getViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        val arrowBack = findViewById<ImageButton>(R.id.arrowBackPlayer)
        playButton = findViewById(R.id.playButton)
        trackLength = findViewById(R.id.trackLength)

        val track = intent.getSerializableExtra("key_track") as? Track

        if (track != null) {
            setupUi(track)

            if (!track.previewUrl.isNullOrEmpty()) {
                playButton.isEnabled = false
                viewModel.prepareUrl(track.previewUrl)
            } else {
                playButton.isEnabled = false
                Toast.makeText(this, "Audio can not be accessible", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
            finish()
        }

        viewModel.observePlayer().observe(this) { state ->
            render(state)
        }

        arrowBack.setOnClickListener { finish() }
        playButton.setOnClickListener { viewModel.playBackControl() }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    private fun setupUi(track: Track) {
        val trackName = findViewById<TextView>(R.id.musicTitle)
        val trackImage = findViewById<ImageView>(R.id.playerPicture)
        val trackAuthor = findViewById<TextView>(R.id.authorText)
        val trackDuration = findViewById<TextView>(R.id.durationRes)
        val trackAlbome = findViewById<TextView>(R.id.albomeRes)
        val trackRelease = findViewById<TextView>(R.id.yearRes)
        val trackGenre = findViewById<TextView>(R.id.genreRes)
        val trackCountry = findViewById<TextView>(R.id.CountryRes)

        trackName.text = track.trackName
        trackAuthor.text = track.artistName
        trackDuration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        trackAlbome.text = track.collectionName.orEmpty()
        trackRelease.text = track.releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4).orEmpty()
        trackGenre.text = track.primaryGenreName
        trackCountry.text = track.country

        Glide.with(this)
            .load(track.artworkUrl100.replace("100x100bb.jpg", "512x512bb.jpg"))
            .placeholder(R.drawable.audio_player_placeholder)
            .into(trackImage)
    }

    private fun render(state: PlayerState) {
        when (state) {
            is PlayerState.Default -> {
                playButton.isEnabled = false
            }
            is PlayerState.Prepared -> {
                playButton.isEnabled = true
                playButton.setBackgroundResource(R.drawable.ic_play)
                trackLength.text = getString(R.string.track_duration00)
            }
            is PlayerState.Playing -> {
                playButton.isEnabled = true
                playButton.setBackgroundResource(R.drawable.ic_pause)
                trackLength.text = state.currentPosition
            }
            is PlayerState.Paused -> {
                playButton.isEnabled = true
                playButton.setBackgroundResource(R.drawable.ic_play)
                trackLength.text = state.currentPosition
            }
        }
    }
}