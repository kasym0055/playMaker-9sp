package com.example.playlist_maker2.ui.search.models

import com.example.playlist_maker2.domain.models.Track

sealed interface MediaState {
    object Loading: MediaState
    object Empty: MediaState
    data class Content(val tracks: List<Track>) : MediaState
}