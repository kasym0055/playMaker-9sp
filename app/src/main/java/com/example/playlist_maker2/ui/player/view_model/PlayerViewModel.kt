package com.example.playlist_maker2.ui.player.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker2.domain.player.AudioPlayerInteractor
import com.example.playlist_maker2.ui.player.models.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class PlayerViewModel(
    private val audioPlayerInteractor: AudioPlayerInteractor
) : ViewModel() {

    private val playerDataLive = MutableLiveData<PlayerState>(PlayerState.Default)
    private var isPreparationStarted = false
    private var progressJob: Job? = null

    fun observePlayer(): LiveData<PlayerState> = playerDataLive

    fun prepareUrl(previewUrl: String): Boolean {
        if (isPreparationStarted || playerDataLive.value !is PlayerState.Default) return true

        isPreparationStarted = true
        return try {
            audioPlayerInteractor.preparePlayer(
                url = previewUrl,
                onPrepared = {
                    playerDataLive.postValue(PlayerState.Prepared)
                },
                onCompletion = {
                    stopProgressUpdates()
                    playerDataLive.postValue(PlayerState.Prepared)
                }
            )
            true
        } catch (_: Exception) {
            isPreparationStarted = false
            false
        }
    }

    fun playBackControl() {
        when (playerDataLive.value) {
            is PlayerState.Playing -> pausePlayer()
            is PlayerState.Prepared, is PlayerState.Paused -> startPlayer()
            else -> {}
        }
    }

    private fun startPlayer() {
        stopProgressUpdates()
        audioPlayerInteractor.startPlayer()
        playerDataLive.value = PlayerState.Playing(getCurrentFormattedTime())
        progressJob = viewModelScope.launch {
            while (isActive && playerDataLive.value is PlayerState.Playing) {
                delay(PROGRESS_UPDATE_DELAY)
                if (playerDataLive.value is PlayerState.Playing) {
                    playerDataLive.value = PlayerState.Playing(getCurrentFormattedTime())
                }
            }
        }
    }

    fun pausePlayer() {
        audioPlayerInteractor.pausePlayer()
        stopProgressUpdates()
        playerDataLive.value = PlayerState.Paused(getCurrentFormattedTime())
    }

    private fun getCurrentFormattedTime(): String {
        val currentPositionInSeconds = audioPlayerInteractor.getCurrentPosition() / 1000L
        return String.format(
            Locale.getDefault(),
            "%02d:%02d",
            currentPositionInSeconds / 60,
            currentPositionInSeconds % 60
        )
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        stopProgressUpdates()
        audioPlayerInteractor.releasePlayer()
        super.onCleared()
    }

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }
}
