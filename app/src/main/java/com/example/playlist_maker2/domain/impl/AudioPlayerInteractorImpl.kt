package com.example.playlist_maker2.domain.impl

import com.example.playlist_maker2.domain.api.AudioPlayerInteractor
import com.example.playlist_maker2.domain.api.AudioPlayerRepository

class AudioPlayerInteractorImpl(
    private val repository: AudioPlayerRepository
): AudioPlayerInteractor {
    override fun preparePlayer(
        url: String,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit
    ) {
        repository.preparePlayer(url,onPrepared,onCompletion)
    }

    override fun startPlayer() {
        repository.startPlayer()
    }

    override fun pausePlayer() {
        repository.pausePlayer()
    }

    override fun releasePlayer() {
        repository.releasePlayer()
    }

    override fun getCurrentPosition(): Int {
        return repository.getCurrentPosition()
    }
}