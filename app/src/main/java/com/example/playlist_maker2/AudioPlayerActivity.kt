package com.example.playlist_maker2

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class AudioPlayerActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)
        val arrowBack = findViewById<ImageButton>(R.id.arrowBackPlayer)
        val track = intent.getSerializableExtra("key_track") as? Track
        if(track != null){
            val trackName = findViewById<TextView>(R.id.musicTitle)
            val trackImage = findViewById<ImageView>(R.id.playerPicture)
            val trackAuthor = findViewById<TextView>(R.id.authorText)
            val trackDuration = findViewById<TextView>(R.id.durationRes)
            val trackAlbome = findViewById<TextView>(R.id.albomeRes)
            val trackRelease = findViewById<TextView>(R.id.yearRes)
            val trackGenre = findViewById<TextView>(R.id.genreRes)
            val trackCountry = findViewById<TextView>(R.id.CountryRes)

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
        }else{
            Toast.makeText(this,"No data", Toast.LENGTH_SHORT).show()
        }

        arrowBack.setOnClickListener {
            finish()
        }
    }
}