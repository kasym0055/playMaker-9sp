package com.example.playlist_maker2.domain.favorite

import com.example.playlist_maker2.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    suspend fun addTrack(track: Track)
    suspend fun removeTrack(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
    fun observeIsFavorite(trackId: Long): Flow<Boolean>
}
