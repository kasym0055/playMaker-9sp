package com.example.playlist_maker2.ui.player.models

import com.example.playlist_maker2.domain.models.Playlist

data class PlayerPlaylistsState(
    val playlists: List<Playlist> = emptyList(),
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isAdding: Boolean = false
)

sealed interface AddToPlaylistResult {
    data class Added(val playlistName: String) : AddToPlaylistResult
    data class AlreadyAdded(val playlistName: String) : AddToPlaylistResult
    data object Error : AddToPlaylistResult
}
