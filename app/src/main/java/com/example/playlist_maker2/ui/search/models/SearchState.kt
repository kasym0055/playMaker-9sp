package com.example.playlist_maker2.ui.search.models

import com.example.playlist_maker2.domain.models.Track

sealed interface SearchState {
    object Loading: SearchState
    data class Content(val tracks: List<Track>): SearchState
    data class History(val tracks: List<Track>) : SearchState
    data class Error(val errorMessage: String): SearchState
    object Empty: SearchState
}