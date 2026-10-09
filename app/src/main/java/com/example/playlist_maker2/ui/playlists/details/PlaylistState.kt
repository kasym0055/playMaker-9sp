package com.example.playlist_maker2.ui.playlists.details

import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track

sealed interface PlaylistState {
    data object Loading : PlaylistState
    data class Content(val playlist: Playlist, val tracks: List<Track>) : PlaylistState {
        val totalMinutes: Long
            get() = tracks.sumOf { it.trackTimeMillis.coerceAtLeast(0) } / 60_000L
    }
    data object Missing : PlaylistState
    data object Error : PlaylistState
}

sealed interface PlaylistEffect {
    data class Share(val playlist: Playlist, val tracks: List<Track>) : PlaylistEffect
    data object EmptyShare : PlaylistEffect
    data object Deleted : PlaylistEffect
    data object Error : PlaylistEffect
}
