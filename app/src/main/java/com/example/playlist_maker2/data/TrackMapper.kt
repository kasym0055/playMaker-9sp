package com.example.playlist_maker2.data

import com.example.playlist_maker2.data.dto.TrackDto
import com.example.playlist_maker2.domain.models.Track

object TrackMapper {
    fun map(dto: TrackDto): Track? {
        if (dto.trackId == null ||
            dto.trackName == null ||
            dto.artistName == null
        ) {
            return null
        }

        return Track(
            trackId = dto.trackId,
            trackName = dto.trackName,
            collectionName = dto.collectionName,
            artistName = dto.artistName,
            trackTimeMillis = dto.trackTimeMillis ?: 0L,
            releaseDate = dto.releaseDate,
            primaryGenreName = dto.primaryGenreName.orEmpty(),
            country = dto.country.orEmpty(),
            artworkUrl100 = dto.artworkUrl100.orEmpty(),
            previewUrl = dto.previewUrl
        )
    }

    fun mapList(dtos: List<TrackDto>): List<Track>{
        return dtos.mapNotNull { map(it) }
    }
}
