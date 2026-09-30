package com.example.playlist_maker2.ui.media.favorite.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker2.domain.favorite.FavoriteTracksInteractor
import com.example.playlist_maker2.ui.media.favorite.models.FavoriteTracksState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(private val interactor: FavoriteTracksInteractor) : ViewModel() {
    private val state = MutableLiveData<FavoriteTracksState>(FavoriteTracksState.Loading)
    private var observationJob: Job? = null

    init { loadFavorites() }

    fun observeState(): LiveData<FavoriteTracksState> = state

    fun loadFavorites() {
        observationJob?.cancel()
        state.value = FavoriteTracksState.Loading
        observationJob = viewModelScope.launch {
            interactor.getFavoriteTracks()
                .catch { error ->
                    if (error is CancellationException) throw error
                    state.value = FavoriteTracksState.Error
                }
                .collect { tracks ->
                    state.value = if (tracks.isEmpty()) FavoriteTracksState.Empty
                    else FavoriteTracksState.Content(tracks)
                }
        }
    }
}
