package com.example.playlist_maker2.ui.media.favorite.models

import com.example.playlist_maker2.domain.models.Track

sealed interface FavoriteTracksState {
    data object Loading : FavoriteTracksState
    data object Empty : FavoriteTracksState
    data object Error : FavoriteTracksState
    data class Content(val tracks: List<Track>) : FavoriteTracksState
}
