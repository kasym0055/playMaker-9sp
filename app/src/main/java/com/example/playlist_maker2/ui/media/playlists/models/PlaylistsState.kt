package com.example.playlist_maker2.ui.media.playlists.models

import com.example.playlist_maker2.domain.models.Playlist

sealed interface PlaylistsState {
    data object Loading : PlaylistsState
    data object Empty : PlaylistsState
    data class Content(val playlists: List<Playlist>) : PlaylistsState
    data object Error : PlaylistsState
}
