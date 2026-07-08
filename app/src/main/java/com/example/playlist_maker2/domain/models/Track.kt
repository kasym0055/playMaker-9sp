package com.example.playlist_maker2.domain.models

import java.io.Serializable

class Track(val trackId: Int,
            val trackName: String ,
            val collectionName: String?,
            val artistName: String ,
            val trackTimeMillis: Long,
            val releaseDate: String?,
            val primaryGenreName: String,
            val country: String,
            val artworkUrl100: String,
            val previewUrl: String?): Serializable