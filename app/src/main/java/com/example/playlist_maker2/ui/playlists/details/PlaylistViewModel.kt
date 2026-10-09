package com.example.playlist_maker2.ui.playlists.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {
    private val stateLiveData = MutableLiveData<PlaylistState>(PlaylistState.Loading)
    private val effectLiveData = MutableLiveData<PlaylistEffect?>()
    private val operationLiveData = MutableLiveData(false)
    private var observationJob: Job? = null
    private var playlistId: Long? = null
    private var isDeleting = false
    private var deletionCompleted = false

    fun observeState(): LiveData<PlaylistState> = stateLiveData
    fun observeEffect(): LiveData<PlaylistEffect?> = effectLiveData
    fun observeOperation(): LiveData<Boolean> = operationLiveData

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadPlaylist(id: Long, force: Boolean = false) {
        if (playlistId == id && observationJob?.isActive == true && !force) return
        playlistId = id
        observationJob?.cancel()
        stateLiveData.value = PlaylistState.Loading
        if (id <= 0) {
            stateLiveData.value = PlaylistState.Missing
            return
        }
        observationJob = viewModelScope.launch {
            playlistsInteractor.observePlaylist(id)
                .flatMapLatest { playlist ->
                    if (playlist == null) {
                        flowOf<PlaylistState>(PlaylistState.Missing)
                    } else {
                        playlistsInteractor.getTracks(playlist.trackIds)
                            .map { tracks -> PlaylistState.Content(playlist, tracks) as PlaylistState }
                    }
                }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(PlaylistState.Error)
                }
                .collect { state ->
                    if (state != PlaylistState.Missing || (!isDeleting && !deletionCompleted)) {
                        stateLiveData.value = state
                    }
                }
        }
    }

    fun sharePlaylist() {
        if (operationLiveData.value == true) return
        val content = stateLiveData.value as? PlaylistState.Content ?: return
        effectLiveData.value = if (content.tracks.isEmpty()) {
            PlaylistEffect.EmptyShare
        } else {
            PlaylistEffect.Share(content.playlist, content.tracks)
        }
    }

    fun removeTrack(trackId: Long) {
        val id = playlistId ?: return
        val content = stateLiveData.value as? PlaylistState.Content ?: return
        if (operationLiveData.value == true || content.tracks.none { it.trackId == trackId }) return
        operationLiveData.value = true
        viewModelScope.launch {
            try {
                playlistsInteractor.removeTrack(id, trackId)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                effectLiveData.value = PlaylistEffect.Error
            } finally {
                operationLiveData.value = false
            }
        }
    }

    fun deletePlaylist() {
        val id = playlistId ?: return
        if (operationLiveData.value == true || stateLiveData.value !is PlaylistState.Content) return
        operationLiveData.value = true
        isDeleting = true
        viewModelScope.launch {
            try {
                playlistsInteractor.deletePlaylist(id)
                deletionCompleted = true
                effectLiveData.value = PlaylistEffect.Deleted
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                effectLiveData.value = PlaylistEffect.Error
            } finally {
                isDeleting = false
                operationLiveData.value = false
            }
        }
    }

    fun consumeEffect() {
        effectLiveData.value = null
    }
}
