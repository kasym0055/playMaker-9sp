package com.example.playlist_maker2.presentation

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.playlist_maker2.Creator
import com.example.playlist_maker2.R
import com.example.playlist_maker2.domain.api.AudioPlayerInteractor
import com.example.playlist_maker2.domain.models.Track

class AudioPlayerActivity : AppCompatActivity(){
    private lateinit var playButton: ImageButton
    private lateinit var audioPlayerInteractor: AudioPlayerInteractor
    private val mediaPlayer = MediaPlayer()
    private var playerState = STATE_DEFAULT
    private var url:String? =null
    private var mainThreadHandler: Handler? = null
    private var trackLength: TextView? =null

    private val timerRunnable = object : Runnable{
        override fun run() {
            if (playerState == STATE_PLAYING){
                val currentPositionInSeconds = audioPlayerInteractor.getCurrentPosition()/TIME_DELAY
                trackLength?.text = String.format("%d:%02d", currentPositionInSeconds/60, currentPositionInSeconds%60)
                mainThreadHandler?.postDelayed(this,TIME_DELAY)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?)  {
        Log.d("TEST", "PLAYER OPENED")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)
        val arrowBack = findViewById<ImageButton>(R.id.arrowBackPlayer)
        val track = intent.getSerializableExtra("key_track") as? Track
        playButton = findViewById(R.id.playButton)
        mainThreadHandler = Handler(Looper.getMainLooper())

        audioPlayerInteractor = Creator.provideAudioPlayerInteractor()


        if(track != null){
            val trackName = findViewById<TextView>(R.id.musicTitle)
            val trackImage = findViewById<ImageView>(R.id.playerPicture)
            val trackAuthor = findViewById<TextView>(R.id.authorText)
            val trackDuration = findViewById<TextView>(R.id.durationRes)
            val trackAlbome = findViewById<TextView>(R.id.albomeRes)
            val trackRelease = findViewById<TextView>(R.id.yearRes)
            val trackGenre = findViewById<TextView>(R.id.genreRes)
            val trackCountry = findViewById<TextView>(R.id.CountryRes)
            trackLength = findViewById<TextView>(R.id.trackLength)

            url = track.previewUrl

            trackName.text = track.trackName
            Glide.with(this)
                .load(track.artworkUrl100.replace("100x100bb.jpg","512x512bb.jpg"))
                .placeholder(R.drawable.audio_player_placeholder)
                .into(trackImage)

            trackAuthor.text = track.artistName
            val minutes = track.trackTimeMillis /1000 / 60
            val seconds = track.trackTimeMillis /1000 % 60
            trackDuration.text = String.format("%d:%02d", minutes, seconds)
            trackAlbome.text = track.collectionName ?: ""
            val date = track.releaseDate ?: ""
            if (!date.isNullOrEmpty() && date.length>=4){
                trackRelease.text = date.substring(0,4)
            }
            else{
                trackRelease.text=""
            }
            trackGenre.text = track.primaryGenreName
            trackCountry.text = track.country

            if (!url.isNullOrEmpty()){
                playButton.isEnabled =false
                prepareUrl()
            }else{
                playButton.isEnabled =false
                Toast.makeText(this,"Audio can not be accessible", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this,"No data", Toast.LENGTH_SHORT).show()
            finish()
        }

        arrowBack.setOnClickListener {
            finish()
        }


        playButton.setOnClickListener {
            playBackControl()
        }

    }

    // play music part
    override fun onPause() {
        super.onPause()
        audioPlayerInteractor.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerInteractor.releasePlayer()
    }
    private fun playBackControl(){
        when(playerState){
            STATE_PLAYING ->{
                stopPlay()
            }
            STATE_PREPARED, STATE_PAUSED->{
                startPlay()
            }
        }
    }
    private fun prepareUrl(){
        audioPlayerInteractor.preparePlayer(
            url=url!!,
            onPrepared = {
                playButton.isEnabled =true
                playerState = STATE_PREPARED
            },
            onCompletion = {
                playButton.setBackgroundResource(R.drawable.ic_pause)
                playerState = STATE_PREPARED
                mainThreadHandler?.removeCallbacks(timerRunnable)
                trackLength?.text = getString(R.string.track_duration00)
            }
        )
    }
    private fun startPlay(){
        audioPlayerInteractor.startPlayer()
        playButton.setBackgroundResource(R.drawable.ic_play)
        playerState = STATE_PLAYING
        mainThreadHandler?.post(timerRunnable)
    }

    private fun stopPlay(){
        audioPlayerInteractor.pausePlayer()
        playButton.setBackgroundResource(R.drawable.ic_pause)
        playerState = STATE_PAUSED
        mainThreadHandler?.removeCallbacks(timerRunnable)
    }



    companion object{
        private const val STATE_DEFAULT = 0
        const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIME_DELAY = 1000L
    }
}