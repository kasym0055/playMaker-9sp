package com.example.playlist_maker2.ui.player.view_model

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlist_maker2.creator.Creator
import com.example.playlist_maker2.domain.player.AudioPlayerInteractor
import com.example.playlist_maker2.ui.player.models.PlayerState
import java.util.Locale

class PlayerViewModel(
    private val audioPlayerInteractor: AudioPlayerInteractor
) : ViewModel() {

    private val playerDataLive = MutableLiveData<PlayerState>(PlayerState.Default)
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (playerDataLive.value is PlayerState.Playing) {
                playerDataLive.value = PlayerState.Playing(getCurrentFormattedTime())
                mainThreadHandler.postDelayed(this, TIMER_UPDATE_DELAY)
            }
        }
    }

    fun observePlayer(): LiveData<PlayerState> = playerDataLive

    fun prepareUrl(previewUrl: String) {
        audioPlayerInteractor.preparePlayer(
            url = previewUrl,
            onPrepared = {
                playerDataLive.value = PlayerState.Prepared
            },
            onCompletion = {
                mainThreadHandler.removeCallbacks(timerRunnable)
                playerDataLive.value = PlayerState.Prepared
            }
        )
    }

    fun playBackControl() {
        when (playerDataLive.value) {
            is PlayerState.Playing -> pausePlayer()
            is PlayerState.Prepared, is PlayerState.Paused -> startPlayer()
            else -> {}
        }
    }

    private fun startPlayer() {
        mainThreadHandler.removeCallbacks(timerRunnable)
        audioPlayerInteractor.startPlayer()
        playerDataLive.value = PlayerState.Playing(getCurrentFormattedTime())
        mainThreadHandler.post(timerRunnable)
    }

    fun pausePlayer() {
        audioPlayerInteractor.pausePlayer()
        mainThreadHandler.removeCallbacks(timerRunnable)
        playerDataLive.value = PlayerState.Paused(getCurrentFormattedTime())
    }

    private fun getCurrentFormattedTime(): String {
        val currentPositionInSeconds = audioPlayerInteractor.getCurrentPosition() / 1000L
        return String.Companion.format(Locale.getDefault(), "%d:%02d", currentPositionInSeconds / 60, currentPositionInSeconds % 60)
    }

    override fun onCleared() {
        super.onCleared()
        mainThreadHandler.removeCallbacks(timerRunnable)
        audioPlayerInteractor.releasePlayer()
    }

    companion object {
        private const val TIMER_UPDATE_DELAY = 300L

        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val playerInteractor = Creator.provideAudioPlayerInteractor()
                PlayerViewModel(playerInteractor)
            }
        }
    }
}