package com.example.playlist_maker2.data

import com.example.playlist_maker2.data.dto.TrackDto
import com.example.playlist_maker2.domain.models.Track

object TrackMapper {
    fun map(dto: TrackDto): Track{
        return Track(
            trackId = dto.trackId ?: 0 ,
            trackName = dto.trackName,
            collectionName = dto.collectionName,
            artistName = dto.artistName,
            trackTimeMillis = dto.trackTimeMillis,
            releaseDate = dto.releaseDate,
            primaryGenreName = dto.primaryGenreName,
            country = dto.country,
            artworkUrl100 = dto.artworkUrl100,
            previewUrl = dto.previewUrl
        )
    }

    fun mapList(dtos: List<TrackDto>): List<Track>{
        return dtos.map { map(it) }
    }
}