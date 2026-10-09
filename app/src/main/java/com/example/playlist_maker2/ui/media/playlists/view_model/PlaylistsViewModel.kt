package com.example.playlist_maker2.ui.media.playlists.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import com.example.playlist_maker2.ui.media.playlists.models.PlaylistsState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PlaylistsViewModel(private val interactor: PlaylistsInteractor) : ViewModel() {
    private val state = MutableLiveData<PlaylistsState>(PlaylistsState.Loading)
    private var observationJob: Job? = null

    init {
        loadPlaylists()
    }

    fun observeState(): LiveData<PlaylistsState> = state

    fun loadPlaylists() {
        observationJob?.cancel()
        state.value = PlaylistsState.Loading
        observationJob = viewModelScope.launch {
            interactor.observePlaylists()
                .catch { error ->
                    if (error is CancellationException) throw error
                    state.value = PlaylistsState.Error
                }
                .collect { playlists ->
                    state.value = if (playlists.isEmpty()) PlaylistsState.Empty
                    else PlaylistsState.Content(playlists)
                }
        }
    }
}
