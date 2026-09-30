package com.example.playlist_maker2.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_tracks")
data class TrackEntity(
    @PrimaryKey val trackId: Long,
    val trackName: String,
    val collectionName: String?,
    val artistName: String,
    val trackTimeMillis: Long,
    val releaseDate: String?,
    val primaryGenreName: String,
    val country: String,
    val artworkUrl100: String,
    val previewUrl: String?,
    val addedAt: Long
)
