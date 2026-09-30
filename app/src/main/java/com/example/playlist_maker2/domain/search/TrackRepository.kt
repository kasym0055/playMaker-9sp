package com.example.playlist_maker2.domain.search

import com.example.playlist_maker2.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun searchTracks(expression: String): Flow<Result<List<Track>>>
}
