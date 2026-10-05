package com.example.playlist_maker2.data.db

import com.example.playlist_maker2.domain.models.Track

object TrackDbMapper {
    fun toEntity(track: Track, addedAt: Long) = TrackEntity(
        track.trackId, track.trackName, track.collectionName, track.artistName,
        track.trackTimeMillis, track.releaseDate, track.primaryGenreName,
        track.country, track.artworkUrl100, track.previewUrl, addedAt
    )

    fun toTrack(entity: TrackEntity) = Track(
        entity.trackId, entity.trackName, entity.collectionName, entity.artistName,
        entity.trackTimeMillis, entity.releaseDate, entity.primaryGenreName,
        entity.country, entity.artworkUrl100, entity.previewUrl, isFavorite = true
    )
}
