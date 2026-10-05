package com.example.playlist_maker2.domain.playlists

import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    fun observePlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long
    suspend fun addTrack(playlistId: Long, track: Track): Boolean
    suspend fun updatePlaylist(playlist: Playlist)
}
