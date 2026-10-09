package com.example.playlist_maker2.domain.playlists.impl

import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import com.example.playlist_maker2.domain.playlists.PlaylistsRepository

class PlaylistsInteractorImpl(private val repository: PlaylistsRepository) : PlaylistsInteractor {
    override fun observePlaylists() = repository.observePlaylists()

    override suspend fun createPlaylist(name: String, description: String, coverUri: String?) =
        repository.createPlaylist(name, description, coverUri)

    override suspend fun addTrack(playlistId: Long, track: Track) =
        repository.addTrack(playlistId, track)

    override suspend fun updatePlaylist(playlist: Playlist) = repository.updatePlaylist(playlist)
}
