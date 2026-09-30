package com.example.playlist_maker2.domain.favorite.impl

import com.example.playlist_maker2.domain.favorite.FavoriteTracksInteractor
import com.example.playlist_maker2.domain.favorite.FavoriteTracksRepository
import com.example.playlist_maker2.domain.models.Track

class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository
) : FavoriteTracksInteractor {
    override suspend fun addTrack(track: Track) = repository.addTrack(track)
    override suspend fun removeTrack(track: Track) = repository.removeTrack(track)
    override fun getFavoriteTracks() = repository.getFavoriteTracks()
    override fun observeIsFavorite(trackId: Long) = repository.observeIsFavorite(trackId)
}
