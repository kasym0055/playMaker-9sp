package com.example.playlist_maker2.ui.playlists.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import com.example.playlist_maker2.ui.playlists.create.CreatePlaylistViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    playlistsInteractor: PlaylistsInteractor,
    savedStateHandle: SavedStateHandle
) : CreatePlaylistViewModel(playlistsInteractor, savedStateHandle) {
    private val playlistId = savedStateHandle.get<Long>(PLAYLIST_ID) ?: 0L

    init {
        if (savedStateHandle.get<Boolean>(INITIALIZED) != true) loadPlaylist()
    }

    private fun loadPlaylist() {
        state.value = state.value!!.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val playlist = requireNotNull(playlistsInteractor.getPlaylist(playlistId))
                savedStateHandle[INITIALIZED] = true
                replaceDraft(playlist.name, playlist.description, playlist.coverPath)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                state.value = state.value!!.copy(isLoading = false, hasLoadError = true)
            }
        }
    }

    fun savePlaylist() = createPlaylist()

    override suspend fun persistPlaylist(name: String, description: String, coverUri: String?) {
        playlistsInteractor.editPlaylist(playlistId, name, description, coverUri)
    }

    companion object {
        private const val PLAYLIST_ID = "playlistId"
        private const val INITIALIZED = "edit_playlist_initialized"
    }
}
