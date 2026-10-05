package com.example.playlist_maker2.data.db

import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistDbMapper(private val gson: Gson) {
    private val trackIdsType = object : TypeToken<List<Long>>() {}.type

    fun toPlaylist(entity: PlaylistEntity) = Playlist(
        entity.id,
        entity.name,
        entity.description,
        entity.coverPath,
        gson.fromJson(entity.trackIds, trackIdsType),
        entity.trackCount
    )

    fun toEntity(playlist: Playlist) = PlaylistEntity(
        playlist.id,
        playlist.name,
        playlist.description,
        playlist.coverPath,
        gson.toJson(playlist.trackIds),
        playlist.trackCount
    )

    fun toTrackEntity(track: Track) = PlaylistTrackEntity(
        track.trackId,
        track.trackName,
        track.collectionName,
        track.artistName,
        track.trackTimeMillis,
        track.releaseDate,
        track.primaryGenreName,
        track.country,
        track.artworkUrl100,
        track.previewUrl,
        System.currentTimeMillis()
    )
}
