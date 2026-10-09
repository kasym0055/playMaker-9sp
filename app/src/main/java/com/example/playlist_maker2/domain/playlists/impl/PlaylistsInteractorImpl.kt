package com.example.playlist_maker2.domain.playlists.impl

import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import com.example.playlist_maker2.domain.playlists.PlaylistsRepository

class PlaylistsInteractorImpl(private val repository: PlaylistsRepository) : PlaylistsInteractor {
    override fun observePlaylists() = repository.observePlaylists()

    override fun observePlaylist(playlistId: Long) = repository.observePlaylist(playlistId)

    override suspend fun getPlaylist(playlistId: Long) = repository.getPlaylist(playlistId)

    override fun getTracks(trackIds: List<Long>) = repository.getTracks(trackIds)

    override suspend fun createPlaylist(name: String, description: String, coverUri: String?) =
        repository.createPlaylist(name, description, coverUri)

    override suspend fun addTrack(playlistId: Long, track: Track) =
        repository.addTrack(playlistId, track)

    override suspend fun updatePlaylist(playlist: Playlist) = repository.updatePlaylist(playlist)

    override suspend fun removeTrack(playlistId: Long, trackId: Long) =
        repository.removeTrack(playlistId, trackId)

    override suspend fun deletePlaylist(playlistId: Long) = repository.deletePlaylist(playlistId)

    override suspend fun editPlaylist(playlistId: Long, name: String, description: String, coverUri: String?) =
        repository.editPlaylist(playlistId, name, description, coverUri)
}
