package com.example.playlist_maker2.domain.playlists

import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    fun observePlaylists(): Flow<List<Playlist>>
    fun observePlaylist(playlistId: Long): Flow<Playlist?>
    suspend fun getPlaylist(playlistId: Long): Playlist?
    fun getTracks(trackIds: List<Long>): Flow<List<Track>>
    suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long
    suspend fun addTrack(playlistId: Long, track: Track): Boolean
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun removeTrack(playlistId: Long, trackId: Long)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun editPlaylist(playlistId: Long, name: String, description: String, coverUri: String?)
}
