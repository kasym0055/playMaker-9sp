package com.example.playlist_maker2.data

import android.media.MediaPlayer
import com.example.playlist_maker2.presentation.AudioPlayerActivity.Companion.STATE_PREPARED
import com.example.playlist_maker2.R
import com.example.playlist_maker2.domain.api.AudioPlayerRepository

class AudioPlayerRepositoryImpl: AudioPlayerRepository {
    private var mediaPlayer = MediaPlayer()
    override fun preparePlayer(
        url: String,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit
    ) {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()

        mediaPlayer.setOnPreparedListener {
            onPrepared.invoke()
        }

        mediaPlayer.setOnCompletionListener {
            onCompletion.invoke()
        }
    }

    override fun startPlayer() {
        mediaPlayer.start()
    }

    override fun pausePlayer() {
        mediaPlayer.stop()
    }

    override fun releasePlayer() {
        mediaPlayer.release()
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }
}