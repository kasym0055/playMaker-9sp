package com.example.playlist_maker2.ui.player.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker2.domain.player.AudioPlayerInteractor
import com.example.playlist_maker2.ui.player.models.PlayerState
import com.example.playlist_maker2.domain.favorite.FavoriteTracksInteractor
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.player.models.FavoriteState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class PlayerViewModel(
    private val audioPlayerInteractor: AudioPlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val playerDataLive = MutableLiveData<PlayerState>(PlayerState.Default)
    private var isPreparationStarted = false
    private var progressJob: Job? = null

    private val favoriteLiveData = MutableLiveData(FavoriteState())
    private var currentTrack: Track? = null
    private var favoriteObservationJob: Job? = null
    private var isFavoriteUpdateRunning = false

    fun observeFavorite(): LiveData<FavoriteState> = favoriteLiveData

    fun setTrack(track: Track) {
        if (currentTrack?.trackId == track.trackId) return
        currentTrack = track
        favoriteLiveData.value = FavoriteState()
        favoriteObservationJob?.cancel()
        favoriteObservationJob = viewModelScope.launch {
            favoriteTracksInteractor.observeIsFavorite(track.trackId)
                .catch { error ->
                    if (error is CancellationException) throw error
                    favoriteLiveData.value = FavoriteState(hasError = true)
                }
                .collect { isFavorite ->
                    track.isFavorite = isFavorite
                    favoriteLiveData.value = FavoriteState(isFavorite, isFavoriteUpdateRunning)
                }
        }
    }

    fun toggleFavorite() {
        val track = currentTrack ?: return
        if (isFavoriteUpdateRunning || favoriteLiveData.value?.isLoading != false) return
        isFavoriteUpdateRunning = true
        favoriteLiveData.value = favoriteLiveData.value?.copy(isLoading = true, hasError = false)
        viewModelScope.launch {
            try {
                val wasFavorite = favoriteTracksInteractor.observeIsFavorite(track.trackId).first()
                if (wasFavorite) favoriteTracksInteractor.removeTrack(track)
                else favoriteTracksInteractor.addTrack(track)
                track.isFavorite = !wasFavorite
                favoriteLiveData.value = FavoriteState(!wasFavorite, isLoading = false)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                favoriteLiveData.value = favoriteLiveData.value?.copy(isLoading = false, hasError = true)
            } finally {
                isFavoriteUpdateRunning = false
            }
        }
    }

    fun consumeFavoriteError() {
        favoriteLiveData.value = favoriteLiveData.value?.copy(hasError = false)
    }

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
