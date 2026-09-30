package com.example.playlist_maker2.domain.search

import com.example.playlist_maker2.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchTracksInteractor {
    fun search(expression: String): Flow<Result<List<Track>>>
}
